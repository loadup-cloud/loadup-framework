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
package io.github.loadup.components.webmvc;

import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration(
        beforeName = {
            "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration",
            "org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration"
        })
@ConditionalOnClass({HttpSecurity.class, SecurityFilterChain.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "loadup.webmvc.fallback-security", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(FallbackSecurityWebAutoConfiguration.FallbackSecurityProperties.class)
public class FallbackSecurityWebAutoConfiguration {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    SecurityFilterChain loadUpFallbackSecurityFilterChain(HttpSecurity http, FallbackSecurityProperties properties)
            throws Exception {
        return http.authorizeHttpRequests(requests -> {
                    if (!properties.permitGetPaths().isEmpty()) {
                        requests.requestMatchers(
                                        HttpMethod.GET,
                                        properties.permitGetPaths().toArray(String[]::new))
                                .permitAll();
                    }
                    requests.anyRequest().denyAll();
                })
                .build();
    }

    @ConfigurationProperties(prefix = "loadup.webmvc.fallback-security")
    record FallbackSecurityProperties(List<String> permitGetPaths) {
        FallbackSecurityProperties {
            permitGetPaths = permitGetPaths == null ? List.of() : List.copyOf(permitGetPaths);
        }
    }
}
