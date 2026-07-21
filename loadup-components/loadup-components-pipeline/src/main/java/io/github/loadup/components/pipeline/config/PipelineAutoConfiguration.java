package io.github.loadup.components.pipeline.config;

/*-
 * #%L
 * Loadup Components Pipeline
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
