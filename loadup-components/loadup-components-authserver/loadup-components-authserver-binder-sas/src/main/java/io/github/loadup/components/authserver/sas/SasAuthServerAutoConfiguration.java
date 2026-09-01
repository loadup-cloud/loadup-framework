package io.github.loadup.components.authserver.sas;

/*-
 * #%L
 * LoadUp Components AuthServer Binder SAS
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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.github.loadup.components.authserver.jwt.LoadUpJwtTokenCustomizer;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.Client;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * Embedded Spring Authorization Server binder (default).
 *
 * <p>Provides the standard beans required by the Spring Boot authorization server
 * auto-configuration (RegisteredClientRepository, AuthorizationServerSettings, JWKSource) plus
 * the LoadUp claims customizer. Spring Authorization Server then exposes the standard OAuth2
 * endpoints ({@code /oauth2/authorize}, {@code /oauth2/token}, {@code /oauth2/jwks}, ...).
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "loadup.components.authserver",
        name = "binder-type",
        havingValue = "sas",
        matchIfMissing = true)
@EnableConfigurationProperties(LoadUpAuthServerProperties.class)
public class SasAuthServerAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SasAuthServerAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(RegisteredClientRepository.class)
    public RegisteredClientRepository registeredClientRepository(LoadUpAuthServerProperties properties) {
        List<RegisteredClient> clients = properties.getClients().stream()
                .map(SasAuthServerAutoConfiguration::toRegisteredClient)
                .toList();
        return new InMemoryRegisteredClientRepository(clients);
    }

    @Bean
    @ConditionalOnMissingBean(AuthorizationServerSettings.class)
    public AuthorizationServerSettings authorizationServerSettings(LoadUpAuthServerProperties properties) {
        return AuthorizationServerSettings.builder()
                .issuer(properties.getIssuer())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(JWKSource.class)
    public JWKSource<SecurityContext> jwkSource(LoadUpAuthServerProperties properties) {
        RSAKey rsaKey = buildRsaKey(properties);
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2TokenCustomizer.class)
    public OAuth2TokenCustomizer<JwtEncodingContext> loadUpJwtTokenCustomizer() {
        return new LoadUpJwtTokenCustomizer();
    }

    private static RegisteredClient toRegisteredClient(Client client) {
        if (StringUtils.isBlank(client.getClientId())) {
            throw new IllegalArgumentException("loadup.components.authserver.clients[].client-id is required");
        }
        RegisteredClient.Builder builder = RegisteredClient.withId(
                        UUID.randomUUID().toString())
                .clientId(client.getClientId())
                .clientSecret("{noop}" + (client.getClientSecret() == null ? "" : client.getClientSecret()))
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(client.isRequireAuthorizationConsent())
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(client.getAccessTokenTtl())
                        .build());
        if (client.getGrantTypes() != null) {
            client.getGrantTypes().forEach(g -> builder.authorizationGrantType(new AuthorizationGrantType(g)));
        }
        if (client.getRedirectUris() != null) {
            client.getRedirectUris().forEach(builder::redirectUri);
        }
        if (client.getScopes() != null) {
            client.getScopes().forEach(builder::scope);
        }
        return builder.build();
    }

    private static RSAKey buildRsaKey(LoadUpAuthServerProperties properties) {
        String privateKeyBase64 = properties.getJwk().getRsaPrivateKeyBase64();
        String kid = properties.getJwk().getKid();
        try {
            RSAPrivateCrtKey privateKey;
            if (StringUtils.isNotBlank(privateKeyBase64)) {
                PrivateKey decoded = KeyFactory.getInstance("RSA")
                        .generatePrivate(
                                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64)));
                privateKey = (RSAPrivateCrtKey) decoded;
            } else {
                log.warn("loadup.components.authserver.jwk.rsa-private-key is not configured; "
                        + "an ephemeral RSA key will be generated. Tokens become invalid after restart.");
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair keyPair = generator.generateKeyPair();
                privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
            }
            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent()));
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(kid)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build the authorization server JWK", e);
        }
    }
}
