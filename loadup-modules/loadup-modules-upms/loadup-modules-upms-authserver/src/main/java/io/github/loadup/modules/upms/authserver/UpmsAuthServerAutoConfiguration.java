package io.github.loadup.modules.upms.authserver;

/*-
 * #%L
 * LoadUp Cloud
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

import io.github.loadup.modules.upms.client.service.AuthenticationService;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.service.UserPermissionService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Connects UPMS password authentication to the authorization-code login flow. */
@AutoConfiguration
public class UpmsAuthServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(UpmsAuthenticationProvider.class)
    public UpmsAuthenticationProvider upmsAuthenticationProvider(
            AuthenticationService authenticationService,
            RoleGateway roleGateway,
            UserPermissionService permissionService) {
        return new UpmsAuthenticationProvider(authenticationService, roleGateway, permissionService);
    }

    @Bean
    @Order(101)
    public SecurityFilterChain upmsLoginSecurityFilterChain(
            HttpSecurity http, UpmsAuthenticationProvider authenticationProvider) throws Exception {
        http.securityMatcher("/login")
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .formLogin(Customizer.withDefaults());
        return http.build();
    }
}
