package io.github.loadup.components.authorization.config;

/*-
 * #%L
 * LoadUp Components Authorization
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.loadup.components.authorization.AuthorizationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Auto-configuration for LoadUp Authorization.
 *
 * <p>The component is a thin integration on top of Spring Security: it enables method-level
 * security ({@code @PreAuthorize} / {@code @Secured}) and registers a permissive, stateless
 * default filter chain. Enforcement happens at the business method level (the Gateway invokes
 * beans directly, so there is no controller layer to protect). Applications that need
 * request-level rules can define their own {@link SecurityFilterChain}.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.authorization", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableMethodSecurity
@EnableConfigurationProperties(AuthorizationProperties.class)
@SuppressFBWarnings(
        value = "SPRING_CSRF_PROTECTION_DISABLED",
        justification = "Stateless API facade: enforcement happens via @PreAuthorize on business methods, the Gateway"
                + " routes bean calls directly and no controller/session layer exists, so CSRF protection is not"
                + " applicable.")
public class AuthorizationAutoConfiguration {

    /**
     * Default stateless and permissive filter chain.
     *
     * <p>Authorization is expressed with {@code @PreAuthorize} on business methods; this chain
     * only disables CSRF and sessions so the component behaves as a pure API facade.
     */
    @Bean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnProperty(
            prefix = "loadup.authorization",
            name = "default-security-filter-chain",
            havingValue = "true",
            matchIfMissing = true)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }
}
