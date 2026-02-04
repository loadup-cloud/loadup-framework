package io.github.loadup.components.security.config;

/*-
 * #%L
 * LoadUp Components Security
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

//import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Auto Configuration
 *
 * <p>This component ONLY provides method-level authorization support (@PreAuthorize).
 * Authentication is handled by Gateway component, which should populate SecurityContext
 * before business logic execution.</p>
 *
 * <p>Usage in business code:</p>
 * <pre>
 * {@code
 * @PreAuthorize("hasRole('ADMIN')")
 * public void deleteUser(String userId) { ... }
 * }
 * </pre>
 */
@AutoConfiguration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityAutoConfiguration {
    // No SecurityFilterChain - Gateway handles authentication
    // This only enables @PreAuthorize/@PostAuthorize annotations

    /**
     * Configure a permissive SecurityFilterChain.
     *
     * <p>Since Gateway handles authentication, we:</p>
     * <ul>
     *   <li>Allow all requests (authentication done by Gateway)</li>
     *   <li>Disable CSRF (stateless API)</li>
     *   <li>Use STATELESS session</li>
     * </ul>
     *
     * <p>Authorization (@PreAuthorize) still enforced on methods.</p>
     */
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize ->
                authorize.anyRequest().permitAll());

        return http.build();
    }
}
