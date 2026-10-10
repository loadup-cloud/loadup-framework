/*-
 * #%L
 * LoadUp UPMS Authorization Server Adapter
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
package io.github.loadup.modules.upms.authserver;

import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties;
import io.github.loadup.modules.upms.client.facade.AuthenticationFacade;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.service.UserPermissionService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Connects UPMS credential checks to JSON login and optional OAuth2 authorization. */
@AutoConfiguration(afterName = "io.github.loadup.components.authserver.sas.SasAuthServerAutoConfiguration")
public class UpmsAuthServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(UpmsAuthenticationProvider.class)
    public UpmsAuthenticationProvider upmsAuthenticationProvider(
            AuthenticationFacade authenticationService,
            RoleGateway roleGateway,
            UserPermissionService permissionService) {
        return new UpmsAuthenticationProvider(authenticationService, roleGateway, permissionService);
    }

    @Bean
    @Order(101)
    @ConditionalOnProperty(
            prefix = "loadup.security.auth-server",
            name = "protocol-endpoints-enabled",
            havingValue = "true",
            matchIfMissing = true)
    public SecurityFilterChain upmsLoginSecurityFilterChain(
            HttpSecurity http, UpmsAuthenticationProvider authenticationProvider) throws Exception {
        http.securityMatcher("/login")
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .formLogin(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @ConditionalOnBean(JwtEncoder.class)
    public UpmsTokenController upmsTokenController(
            UpmsAuthenticationProvider authenticationProvider,
            JwtEncoder jwtEncoder,
            LoadUpAuthServerProperties properties) {
        return new UpmsTokenController(authenticationProvider, jwtEncoder, properties);
    }
}
