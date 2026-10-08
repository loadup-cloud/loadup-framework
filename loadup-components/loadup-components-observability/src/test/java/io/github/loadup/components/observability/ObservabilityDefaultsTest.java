/*
 * #%L
 * LoadUp Observability
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
package io.github.loadup.components.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class ObservabilityDefaultsTest {
    private final ObservabilityEnvironmentPostProcessor processor = new ObservabilityEnvironmentPostProcessor();

    @Test
    void usesApplicationNameOrFixedFallback() {
        var environment = new MockEnvironment().withProperty("spring.application.name", "merchant");
        processor.postProcessEnvironment(environment, new SpringApplication());
        assertThat(environment.getProperty("management.metrics.tags.application"))
                .isEqualTo("merchant");
        var unnamed = new MockEnvironment();
        processor.postProcessEnvironment(unnamed, new SpringApplication());
        assertThat(unnamed.getProperty("management.metrics.tags.application")).isEqualTo("application");
    }

    @Test
    void explicitManagementTagWins() {
        var environment = new MockEnvironment()
                .withProperty("spring.application.name", "merchant")
                .withProperty("management.metrics.tags.application", "billing");
        processor.postProcessEnvironment(environment, new SpringApplication());
        assertThat(environment.getProperty("management.metrics.tags.application"))
                .isEqualTo("billing");
    }
}
