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

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;

class FallbackSecurityWebAutoConfigurationTest {
    private static final String ENABLED = "loadup.webmvc.fallback-security.enabled=true";
    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FallbackSecurityWebAutoConfiguration.class));

    @Test
    void backsOffByDefaultAndWithoutSpringSecurity() {
        runner.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean("loadUpFallbackSecurityFilterChain"));
        runner.withPropertyValues(ENABLED)
                .withClassLoader(new FilteredClassLoader(HttpSecurity.class))
                .run(context ->
                        assertThat(context).hasNotFailed().doesNotHaveBean("loadUpFallbackSecurityFilterChain"));
    }

    @Test
    void permitsOnlyConfiguredGetsAndLetsSpecificSecurityChainsRunFirst() {
        runner.withUserConfiguration(SecurityFixture.class)
                .withPropertyValues(ENABLED, "loadup.webmvc.fallback-security.permit-get-paths[0]=/actuator/health")
                .run(context -> {
                    var proxy = context.getBean(FilterChainProxy.class);
                    assertThat(status(proxy, "GET", "/actuator/health")).isEqualTo(204);
                    assertThat(status(proxy, "POST", "/actuator/health")).isEqualTo(403);
                    assertThat(status(proxy, "GET", "/actuator/health/readiness"))
                            .isEqualTo(403);
                    assertThat(status(proxy, "GET", "/")).isEqualTo(403);
                    assertThat(status(proxy, "GET", "/api/explicit")).isEqualTo(204);
                });
    }

    private static int status(FilterChainProxy proxy, String method, String path) throws Exception {
        var request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        var response = new MockHttpServletResponse();
        proxy.doFilter(
                request,
                response,
                (ignored, result) -> ((HttpServletResponse) result).setStatus(HttpServletResponse.SC_NO_CONTENT));
        return response.getStatus();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class SecurityFixture {
        @Bean
        @Order(100)
        SecurityFilterChain specificApiChain(HttpSecurity http) throws Exception {
            return http.securityMatcher("/api/explicit")
                    .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                    .build();
        }
    }
}
