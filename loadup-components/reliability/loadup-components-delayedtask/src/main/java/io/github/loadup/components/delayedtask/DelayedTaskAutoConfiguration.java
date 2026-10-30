/*
 * #%L
 * LoadUp Components DelayedTask
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
package io.github.loadup.components.delayedtask;

import io.github.loadup.components.database.autoconfigure.DatabaseCoreAutoConfiguration;
import io.github.loadup.components.database.autoconfigure.IdGeneratorAutoConfiguration;
import io.github.loadup.components.database.id.IdGenerator;
import io.micrometer.observation.ObservationRegistry;
import java.time.Clock;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(
        after = {IdGeneratorAutoConfiguration.class, DatabaseCoreAutoConfiguration.class},
        afterName = "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration")
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnBean({JdbcTemplate.class, IdGenerator.class})
@ConditionalOnProperty(prefix = "loadup.delayedtask", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DelayedTaskProperties.class)
@Import(DelayedTaskConverterImpl.class)
public class DelayedTaskAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DelayedTaskStore delayedTaskStore(JdbcTemplate jdbc, IdGenerator ids) {
        return new DelayedTaskStore(jdbc, ids);
    }

    @Bean
    @ConditionalOnMissingBean
    public DelayedTaskTemplate delayedTaskTemplate(
            DelayedTaskStore store,
            DelayedTaskConverter converter,
            DelayedTaskProperties properties,
            Clock clock,
            DataSource dataSource) {
        return new DelayedTaskTemplate(store, converter, properties, clock, dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public DelayedTaskDispatcher delayedTaskDispatcher(
            DelayedTaskStore store,
            DelayedTaskConverter converter,
            DelayedTaskProperties props,
            Clock clock,
            org.springframework.beans.factory.ObjectProvider<ObservationRegistry> registry,
            List<DelayedTaskHandler> handlers) {
        return new DelayedTaskDispatcher(
                store, converter, props, clock, registry.getIfAvailable(() -> ObservationRegistry.NOOP), handlers);
    }
}
