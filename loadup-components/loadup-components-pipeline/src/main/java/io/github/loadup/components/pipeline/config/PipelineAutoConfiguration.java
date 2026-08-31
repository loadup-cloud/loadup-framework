package io.github.loadup.components.pipeline.config;

/*-
 * #%L
 * Loadup Components Pipeline
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

import io.github.loadup.components.pipeline.engine.PipelineExecutor;
import io.github.loadup.components.pipeline.tx.DefaultSpringTxInitializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for {@code loadup-components-pipeline}.
 *
 * <p>Registers:
 * <ul>
 *   <li>{@link PipelineExecutor} — the core execution engine</li>
 *   <li>{@link DefaultSpringTxInitializer} — default REQUIRED-propagation TX initialiser</li>
 * </ul>
 */
@AutoConfiguration
public class PipelineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PipelineExecutor.class)
    public PipelineExecutor pipelineExecutor(ApplicationContext applicationContext) {
        return new PipelineExecutor(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultSpringTxInitializer.class)
    public DefaultSpringTxInitializer defaultSpringTxInitializer() {
        return new DefaultSpringTxInitializer();
    }
}
