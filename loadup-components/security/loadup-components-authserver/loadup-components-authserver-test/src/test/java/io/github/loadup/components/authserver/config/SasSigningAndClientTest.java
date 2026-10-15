/*
 * #%L
 * LoadUp Components AuthServer Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
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
 * #L%
 */
package io.github.loadup.components.authserver.config;

import static org.assertj.core.api.Assertions.*;

import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.Client;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.Jwk;
import io.github.loadup.components.authserver.sas.SasAuthServerAutoConfiguration;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

class SasSigningAndClientTest {
    private final SasAuthServerAutoConfiguration configuration = new SasAuthServerAutoConfiguration();

    @Test
    void configuredPrivateKeySignsTokensThatLocalDecoderVerifies() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair = generator.generateKeyPair();
        var properties = new LoadUpAuthServerProperties();
        Jwk jwk = new Jwk();
        jwk.setKid("rotation-key");
        jwk.setRsaPrivateKeyBase64(
                Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        properties.setJwk(jwk);
        properties.setUserAccessTokenTtl(Duration.ofMinutes(10));
        properties.setProtocolEndpointsEnabled(false);
        var signingKey = configuration.loadUpSigningKey(properties);
        assertThat(signingKey.toRSAPublicKey()).isEqualTo(pair.getPublic());
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .subject("merchant")
                .issuedAt(now)
                .expiresAt(now.plus(properties.getUserAccessTokenTtl()))
                .build();
        var token = configuration
                .loadUpJwtEncoder(configuration.jwkSource(signingKey))
                .encode(JwtEncoderParameters.from(
                        JwsHeader.with(SignatureAlgorithm.RS256)
                                .keyId(jwk.getKid())
                                .build(),
                        claims));
        assertThat(configuration
                        .loadUpLocalJwtDecoder(signingKey)
                        .decode(token.getTokenValue())
                        .getSubject())
                .isEqualTo("merchant");
        assertThat(properties.isProtocolEndpointsEnabled()).isFalse();
        assertThat(token.getHeaders()).containsEntry("kid", "rotation-key");
    }

    @Test
    void invalidKeyAndMissingAudienceFailStartupWithStableContext() {
        var properties = new LoadUpAuthServerProperties();
        properties.getJwk().setRsaPrivateKeyBase64("invalid-base64!");
        assertThatThrownBy(() -> configuration.loadUpSigningKey(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to build the authorization server JWK");
        assertThatThrownBy(() -> configuration.loadUpJwtTokenCustomizer(properties))
                .isInstanceOf(IllegalArgumentException.class);
        properties.setAudience("  ");
        assertThatThrownBy(() -> configuration.loadUpJwtTokenCustomizer(properties))
                .isInstanceOf(IllegalArgumentException.class);
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
        assertThat(registered.getClientSecret()).startsWith("{bcrypt}");
        assertThat(new BCryptPasswordEncoder()
                        .matches(
                                "synthetic-secret", registered.getClientSecret().substring(8)))
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
    void localTokenModeCreatesDecoderWithoutProtocolClientRepository() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SasAuthServerAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.security.auth-server.protocol-endpoints-enabled=false",
                        "loadup.security.auth-server.audience=merchant-api")
                .run(context -> {
                    assertThat(context)
                            .hasNotFailed()
                            .hasSingleBean(JwtEncoder.class)
                            .hasSingleBean(JwtDecoder.class)
                            .doesNotHaveBean(RegisteredClientRepository.class);
                });
    }

    private static Client client() {
        Client client = new Client();
        client.setClientId("merchant");
        client.setClientSecret("synthetic-secret");
        client.setGrantTypes(List.of("client_credentials"));
        return client;
    }
}
