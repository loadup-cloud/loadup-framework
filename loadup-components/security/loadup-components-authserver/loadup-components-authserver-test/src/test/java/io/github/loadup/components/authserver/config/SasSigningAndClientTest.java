/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.authserver.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.SignedJWT;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.Client;
import io.github.loadup.components.authserver.sas.SasAuthServerAutoConfiguration;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

class SasSigningAndClientTest {
    private final SasAuthServerAutoConfiguration configuration = new SasAuthServerAutoConfiguration();

    private static Client client() {
        Client client = new Client();
        client.setClientId("merchant");
        client.setClientSecret("synthetic-secret");
        client.setGrantTypes(List.of("client_credentials"));
        return client;
    }

    @TempDir
    Path directory;

    private static JwtEncoderParameters tokenParameters() {
        var now = Instant.now();
        return JwtEncoderParameters.from(JwtClaimsSet.builder()
                .issuer("https://auth.test")
                .subject("merchant")
                .audience(List.of("merchant-api"))
                .issuedAt(now)
                .notBefore(now.minusSeconds(1))
                .expiresAt(now.plusSeconds(600))
                .claim("token_use", "access")
                .claim("permissions", List.of("merchant:read"))
                .build());
    }

    @SuppressWarnings("unchecked")
    @Test
    void persistentKmsKeySignsTokensAcrossContextRestartsAndJwksContainsOnlyPublicMaterial() throws Exception {
        var file = TestKeyStores.create(directory, "auth-current");
        var encoded = new AtomicReference<String>();
        TestKeyStores.runner(file).run(context -> {
            assertThat(context).hasNotFailed().doesNotHaveBean(RSAKey.class);
            var token = context.getBean(JwtEncoder.class).encode(tokenParameters());
            encoded.set(token.getTokenValue());
            assertThat(token.getHeaders())
                    .containsEntry("kid", "auth-current-v1")
                    .containsEntry("alg", "RS256");
            assertThat(context.getBean(JwtDecoder.class)
                            .decode(token.getTokenValue())
                            .getSubject())
                    .isEqualTo("merchant");
            var source = (JWKSource<SecurityContext>) context.getBean(JWKSource.class);
            var keys = source.get(new JWKSelector(new JWKMatcher.Builder().build()), null);
            assertThat(keys).hasSize(1);
            assertThat(keys.getFirst().isPrivate()).isFalse();
            assertThat(keys.getFirst().toJSONString()).doesNotContain("\"d\":", "\"p\":", "\"q\":");
            assertThat(context).doesNotHaveBean(RegisteredClientRepository.class);
        });
        TestKeyStores.runner(file).run(context -> {
            var decoded = context.getBean(JwtDecoder.class).decode(encoded.get());
            assertThat(decoded.getAudience()).containsExactly("merchant-api");
            assertThat(decoded.getClaimAsStringList("permissions")).containsExactly("merchant:read");
            assertThat(decoded.getIssuedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
            assertThat(decoded.getNotBefore()).isNotNull().isBefore(Instant.now());
            assertThat(decoded.getExpiresAt()).isNotNull().isAfter(Instant.now());
        });
    }

    @Test
    void springTypedRs256HeaderIsSerializedAndSignedWithItsJsonAlgorithmName() throws Exception {
        var file = TestKeyStores.create(directory, "auth-current");
        TestKeyStores.runner(file).run(context -> {
            var header = JwsHeader.with(SignatureAlgorithm.RS256).type("JWT").build();
            assertThat(header.getHeaders()).containsEntry("alg", SignatureAlgorithm.RS256);
            var encoded = context.getBean(JwtEncoder.class)
                    .encode(JwtEncoderParameters.from(header, tokenParameters().getClaims()));
            var parsed = SignedJWT.parse(encoded.getTokenValue());
            assertThat(parsed.getHeader().getAlgorithm().getName()).isEqualTo("RS256");
            assertThat(parsed.getHeader().getType().toString()).isEqualTo("JWT");
            assertThat(parsed.getHeader().getKeyID()).isEqualTo("auth-current-v1");
            assertThat(encoded.getHeaders()).containsEntry("alg", "RS256").containsEntry("typ", "JWT");
            assertThat(context.getBean(JwtDecoder.class)
                            .decode(encoded.getTokenValue())
                            .getSubject())
                    .isEqualTo("merchant");
        });
    }

    @Test
    void missingKmsMissingAliasAndMissingAudienceFailWithoutGeneratingReplacementKeys() throws Exception {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SasAuthServerAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.security.auth-server.protocol-endpoints-enabled=false",
                        "loadup.security.auth-server.audience=merchant-api")
                .run(context -> assertThat(context).hasFailed());
        var file = TestKeyStores.create(directory, "auth-current");
        TestKeyStores.runner(file)
                .withPropertyValues("loadup.security.auth-server.signing.active-key.name=missing")
                .run(context -> assertThat(context).hasFailed());
        TestKeyStores.runner(file)
                .withPropertyValues("loadup.security.auth-server.signing.active-key.version=0")
                .run(context -> assertThat(context).hasFailed());
        TestKeyStores.runner(file)
                .withPropertyValues("loadup.security.auth-server.audience=")
                .run(context -> assertThat(context).hasFailed());
        var properties = new LoadUpAuthServerProperties();
        assertThatThrownBy(() -> configuration.loadUpJwtTokenCustomizer(properties))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rotationSignsWithNewKeyAndRetainsOldPublicKeyForExistingTokens() throws Exception {
        var file = TestKeyStores.create(directory, "auth-current", "auth-next");
        var oldToken = new AtomicReference<String>();
        TestKeyStores.runner(file)
                .run(context -> oldToken.set(context.getBean(JwtEncoder.class)
                        .encode(tokenParameters())
                        .getTokenValue()));
        TestKeyStores.runner(file)
                .withPropertyValues(
                        "loadup.security.auth-server.signing.active-key.name=auth-next",
                        "loadup.security.auth-server.signing.verification-keys[0].name=auth-current")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    var next = context.getBean(JwtEncoder.class).encode(tokenParameters());
                    assertThat(next.getHeaders()).containsEntry("kid", "auth-next-v1");
                    assertThat(context.getBean(JwtDecoder.class)
                                    .decode(oldToken.get())
                                    .getSubject())
                            .isEqualTo("merchant");
                    assertThat(context.getBean(JwtDecoder.class)
                                    .decode(next.getTokenValue())
                                    .getSubject())
                            .isEqualTo("merchant");
                    var parameters = JwtEncoderParameters.from(
                            JwsHeader.with(SignatureAlgorithm.RS256)
                                    .keyId("auth-current-v1")
                                    .build(),
                            tokenParameters().getClaims());
                    assertThatThrownBy(() -> context.getBean(JwtEncoder.class).encode(parameters))
                            .isInstanceOf(JwtEncodingException.class);
                });
        TestKeyStores.runner(file)
                .withPropertyValues("loadup.security.auth-server.signing.active-key.name=auth-next")
                .run(context -> assertThatThrownBy(
                                () -> context.getBean(JwtDecoder.class).decode(oldToken.get()))
                        .isInstanceOf(JwtException.class));
    }

    @Test
    void clientRegistrationEncryptsSecretAndHonorsConsentAndLifetime() {
        var properties = new LoadUpAuthServerProperties();
        var client = client();
        client.setRedirectUris(null);
        client.setScopes(null);
        client.setRequireAuthorizationConsent(true);
        client.setAccessTokenTtl(Duration.ofMinutes(7));
        properties.setClients(List.of(client));
        var registered = configuration.registeredClientRepository(properties).findByClientId("merchant");
        assertThat(registered).isNotNull();
        assertThat(registered.getClientSettings().isRequireAuthorizationConsent())
                .isTrue();
        assertThat(registered.getTokenSettings().getAccessTokenTimeToLive()).isEqualTo(Duration.ofMinutes(7));
        assertThat(registered.getRedirectUris()).isEmpty();
        assertThat(registered.getScopes()).isEmpty();
        String encodedSecret = Objects.requireNonNull(registered.getClientSecret());
        assertThat(encodedSecret).startsWith("{bcrypt}");
        assertThat(new BCryptPasswordEncoder().matches("synthetic-secret", encodedSecret.substring(8)))
                .isTrue();
    }

    @Test
    void invalidClientConfigurationsAreRejectedBeforeRepositoryCreation() {
        for (String missing : new String[] {"id", "secret", "grants"}) {
            var properties = new LoadUpAuthServerProperties();
            var client = client();
            switch (missing) {
                case "id" -> client.setClientId(" ");
                case "secret" -> client.setClientSecret(null);
                case "grants" -> client.setGrantTypes(null);
                default -> throw new AssertionError(missing);
            }
            properties.setClients(List.of(client));
            assertThatThrownBy(() -> configuration.registeredClientRepository(properties))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void localTokenModeCreatesDecoderWithoutProtocolClientRepository() throws Exception {
        var file = TestKeyStores.create(directory, "auth-current");
        TestKeyStores.runner(file).run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(JwtEncoder.class)
                    .hasSingleBean(JwtDecoder.class)
                    .doesNotHaveBean(RegisteredClientRepository.class);
        });
    }
}
