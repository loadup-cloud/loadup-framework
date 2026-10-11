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

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Uses Spring Boot's MeterRegistry and tracing auto-configuration as the single backend. */
@AutoConfiguration
public class ObservabilityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(LoadUpContextTaskDecorator.class)
    public LoadUpContextTaskDecorator loadUpContextTaskDecorator() {
        return new LoadUpContextTaskDecorator();
    }

    @Bean
    public ApiResultMetrics apiResultMetrics(MeterRegistry registry) {
        return new ApiResultMetrics(registry);
    }
}
