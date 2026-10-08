/*
 * #%L
 * LoadUp Http
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
package io.github.loadup.components.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class HttpAutoConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HttpAutoConfiguration.class))
            .withBean(RestClient.Builder.class, RestClient::builder)
            .withBean(ObjectMapper.class, () -> JsonMapper.builder().build());

    @Test
    void bindsNamedClientsAndOperationsWithDefaults() {
        context.withPropertyValues(
                        "loadup.http.clients.demo.base-url=https://example.test",
                        "loadup.http.clients.demo.operations.query.method=POST",
                        "loadup.http.clients.demo.operations.query.path=/orders/{id}")
                .run(application -> {
                    assertThat(application).hasSingleBean(HttpTemplate.class);
                    var properties = application.getBean(HttpProperties.class);
                    assertThat(properties.clients().get("demo").readTimeout())
                            .isEqualTo(java.time.Duration.ofSeconds(10));
                    assertThat(properties.clients().get("demo").operations()).containsKey("query");
                });
    }

    @Test
    void disableSwitchPreventsAssembly() {
        context.withPropertyValues("loadup.http.enabled=false")
                .run(application -> assertThat(application).doesNotHaveBean(HttpTemplate.class));
    }
}
