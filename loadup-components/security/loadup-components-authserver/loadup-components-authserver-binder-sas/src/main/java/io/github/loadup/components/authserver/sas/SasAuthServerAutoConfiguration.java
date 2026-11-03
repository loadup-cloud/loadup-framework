/*
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
package io.github.loadup.components.authserver.sas;

import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.github.loadup.components.authserver.jwt.LoadUpJwtTokenCustomizer;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties;
import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties.Client;
import io.github.loadup.components.kms.KmsTemplate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
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
        afterName = {
            "io.github.loadup.components.kms.LocalKmsAutoConfiguration",
            "io.github.loadup.components.kms.KmsAutoConfiguration"
        },
        beforeName = {
            "org.springframework.boot.security.oauth2.server.authorization.autoconfigure.servlet.OAuth2AuthorizationServerAutoConfiguration",
            "org.springframework.boot.security.oauth2.server.authorization.autoconfigure.servlet.OAuth2AuthorizationServerJwtAutoConfiguration",
            "io.github.loadup.components.resourceserver.ResourceServerAutoConfiguration"
        })
@EnableConfigurationProperties(LoadUpAuthServerProperties.class)
public class SasAuthServerAutoConfiguration {

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

    private static String encodeClientSecret(String secret) {
        if (StringUtils.isBlank(secret)) {
            throw new IllegalArgumentException("loadup.security.auth-server.clients[].client-secret is required");
        }
        return "{bcrypt}" + new BCryptPasswordEncoder().encode(secret);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(
            prefix = "loadup.security.auth-server",
            name = "protocol-endpoints-enabled",
            havingValue = "true",
            matchIfMissing = true)
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
    @ConditionalOnProperty(
            prefix = "loadup.security.auth-server",
            name = "protocol-endpoints-enabled",
            havingValue = "true",
            matchIfMissing = true)
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
    KmsSigningKeys loadUpSigningKeys(ObjectProvider<KmsTemplate> kms, LoadUpAuthServerProperties properties) {
        return new KmsSigningKeys(kms.getObject(), properties.getSigning());
    }

    @Bean
    @ConditionalOnMissingBean(JWKSource.class)
    JWKSource<SecurityContext> jwkSource(KmsSigningKeys keys) {
        return new ImmutableJWKSet<>(keys.publicKeys());
    }

    @Bean
    @ConditionalOnMissingBean(JwtEncoder.class)
    JwtEncoder loadUpJwtEncoder(ObjectProvider<KmsTemplate> kms, KmsSigningKeys keys) {
        return new KmsJwtEncoder(kms.getObject(), keys);
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder loadUpLocalJwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2TokenCustomizer.class)
    public OAuth2TokenCustomizer<JwtEncodingContext> loadUpJwtTokenCustomizer(LoadUpAuthServerProperties properties) {
        if (StringUtils.isBlank(properties.getAudience())) {
            throw new IllegalArgumentException("loadup.security.auth-server.audience is required");
        }
        return new LoadUpJwtTokenCustomizer(properties.getAudience());
    }
}
