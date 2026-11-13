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
package io.github.loadup.components.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.components.authserver.sas.SasAuthServerAutoConfiguration;
import io.github.loadup.components.oauth.autoconfigure.OAuthJdbcAutoConfiguration;
import io.github.loadup.components.observability.ApiResultMetrics;
import io.github.loadup.components.resourceserver.ResourceServerAutoConfiguration;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.json.JsonMapper;

class OAuthAutoConfigurationIsolationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    OAuthJdbcAutoConfiguration.class,
                    SasAuthServerAutoConfiguration.class,
                    ResourceServerAutoConfiguration.class));

    @Test
    void disabledComponentCreatesNoSigningStorageOrProtocolBeans() {
        runner.run(context -> assertThat(context)
                .hasNotFailed()
                .doesNotHaveBean(JwtEncoder.class)
                .doesNotHaveBean(JwtDecoder.class)
                .doesNotHaveBean(RegisteredClientRepository.class)
                .doesNotHaveBean(SecurityFilterChain.class));
    }

    @Test
    void resourceOnlyApplicationNeedsNeitherKmsNorJdbc() {
        new WebApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader("org.springframework.jdbc"))
                .withConfiguration(AutoConfigurations.of(
                        OAuthJdbcAutoConfiguration.class,
                        SasAuthServerAutoConfiguration.class,
                        ResourceServerAutoConfiguration.class))
                .withUserConfiguration(ResourceSupport.class)
                .withPropertyValues(
                        "loadup.security.resource-server.enabled=true",
                        "loadup.security.resource-server.issuer-uri=https://issuer.test",
                        "loadup.security.resource-server.audience=orders")
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .hasSingleBean(JwtDecoder.class)
                        .hasSingleBean(SecurityFilterChain.class)
                        .doesNotHaveBean(JwtEncoder.class)
                        .doesNotHaveBean(RegisteredClientRepository.class));
    }

    @Test
    void jdbcStoresRequireExplicitOptIn() {
        runner.withBean(JdbcOperations.class, () -> mock(JdbcOperations.class))
                .run(context -> assertThat(context).doesNotHaveBean(RegisteredClientRepository.class));
        runner.withBean(JdbcOperations.class, () -> mock(JdbcOperations.class))
                .withPropertyValues("loadup.security.oauth.persistence.enabled=true")
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .hasSingleBean(RegisteredClientRepository.class)
                        .hasSingleBean(OAuth2AuthorizationService.class)
                        .hasSingleBean(OAuth2AuthorizationConsentService.class)
                        .doesNotHaveBean(JwtEncoder.class));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class ResourceSupport {
        @Bean
        JwtDecoder decoder() {
            return mock(JwtDecoder.class);
        }

        @Bean
        JsonMapper mapper() {
            return JsonMapper.builder().build();
        }

        @Bean(destroyMethod = "close")
        SimpleMeterRegistry metrics() {
            return new SimpleMeterRegistry();
        }

        @Bean
        ApiResultMetrics resultMetrics(SimpleMeterRegistry metrics) {
            return new ApiResultMetrics(metrics);
        }
    }
}
