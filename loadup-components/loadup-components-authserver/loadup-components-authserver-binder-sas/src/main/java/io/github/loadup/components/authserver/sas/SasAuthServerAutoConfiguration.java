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
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * Embedded Spring Authorization Server binder (default).
 *
 * <p>Provides the standard authorization server beans and a dedicated filter chain for the
 * protocol endpoints. The chain remains active alongside application-defined security chains.
 */
@AutoConfiguration(
        beforeName = {
            "org.springframework.boot.security.oauth2.server.authorization.autoconfigure.servlet.OAuth2AuthorizationServerAutoConfiguration",
            "org.springframework.boot.security.oauth2.server.authorization.autoconfigure.servlet.OAuth2AuthorizationServerJwtAutoConfiguration",
            "io.github.loadup.components.resourceserver.ResourceServerAutoConfiguration"
        })
@EnableConfigurationProperties(LoadUpAuthServerProperties.class)
public class SasAuthServerAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SasAuthServerAutoConfiguration.class);

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "loadup.security.auth-server", name = "protocol-endpoints-enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain loadUpAuthorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.oauth2AuthorizationServer(authorizationServer -> {
            http.securityMatcher(authorizationServer.getEndpointsMatcher());
            authorizationServer.oidc(Customizer.withDefaults());
        });
        MediaTypeRequestMatcher htmlRequests = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
        htmlRequests.setIgnoredMediaTypes(Set.of(MediaType.ALL));
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"), htmlRequests));
        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean(RegisteredClientRepository.class)
    @ConditionalOnProperty(prefix = "loadup.security.auth-server", name = "protocol-endpoints-enabled", havingValue = "true", matchIfMissing = true)
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
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    @ConditionalOnMissingBean(RSAKey.class)
    public RSAKey loadUpSigningKey(LoadUpAuthServerProperties properties) {
        return buildRsaKey(properties);
    }

    @Bean
    @ConditionalOnMissingBean(JwtEncoder.class)
    public JwtEncoder loadUpJwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    @ConditionalOnProperty(prefix = "loadup.security.auth-server", name = "protocol-endpoints-enabled", havingValue = "false")
    public JwtDecoder loadUpLocalJwtDecoder(RSAKey rsaKey) throws com.nimbusds.jose.JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2TokenCustomizer.class)
    public OAuth2TokenCustomizer<JwtEncodingContext> loadUpJwtTokenCustomizer(LoadUpAuthServerProperties properties) {
        if (StringUtils.isBlank(properties.getAudience())) {
            throw new IllegalArgumentException("loadup.security.auth-server.audience is required");
        }
        return new LoadUpJwtTokenCustomizer(properties.getAudience());
    }

    private static RegisteredClient toRegisteredClient(Client client) {
        if (StringUtils.isBlank(client.getClientId())) {
            throw new IllegalArgumentException("loadup.security.auth-server.clients[].client-id is required");
        }
        RegisteredClient.Builder builder = RegisteredClient.withId(
                        UUID.randomUUID().toString())
                .clientId(client.getClientId())
                .clientSecret(encodeClientSecret(client.getClientSecret()))
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
                log.warn("loadup.security.auth-server.jwk.rsa-private-key-base64 is not configured; "
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

    private static String encodeClientSecret(String secret) {
        if (StringUtils.isBlank(secret)) {
            throw new IllegalArgumentException("loadup.security.auth-server.clients[].client-secret is required");
        }
        return "{bcrypt}" + new BCryptPasswordEncoder().encode(secret);
    }
}
