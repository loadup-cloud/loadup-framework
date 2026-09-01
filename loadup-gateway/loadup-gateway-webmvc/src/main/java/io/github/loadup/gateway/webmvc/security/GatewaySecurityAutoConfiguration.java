package io.github.loadup.gateway.webmvc.security;

/*-
 * #%L
 * Loadup Gateway WebMVC Engine
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

import io.github.loadup.components.authorization.config.AuthorizationAutoConfiguration;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Standard OAuth2 resource server auto-configuration for the gateway.
 *
 * <p>The gateway is stateless and route-level security is enforced by the
 * {@code SecurityHandlerFilterFunction} pipeline, so this chain is deliberately permissive:
 * authentication is established by {@code BearerTokenAuthenticationFilter} + Nimbus
 * {@link JwtDecoder} whenever a bearer token is present, and route security codes decide
 * whether authentication is required. Applications that need request-level rules can define
 * their own {@link SecurityFilterChain}.
 */
@AutoConfiguration
@AutoConfigureBefore(AuthorizationAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(JwtDecoder.class)
@ConditionalOnProperty(
        prefix = "loadup.gateway.security",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewaySecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "loadupJwtAuthenticationConverter")
    public LoadUpJwtAuthenticationConverter loadupJwtAuthenticationConverter() {
        return new LoadUpJwtAuthenticationConverter();
    }

    @Bean
    @ConditionalOnMissingBean(ResourceServerBinder.class)
    public ResourceServerBinder resourceServerBinder(GatewayProperties properties) {
        return new NimbusResourceServerBinder(properties);
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder gatewayJwtDecoder(ResourceServerBinder resourceServerBinder) {
        return resourceServerBinder.jwtDecoder();
    }

    @Bean
    @Order(SecurityFilterProperties.DEFAULT_FILTER_ORDER)
    public SecurityFilterChain gatewaySecurityFilterChain(
            HttpSecurity http, JwtDecoder jwtDecoder, LoadUpJwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }
}
