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

import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.delayedtask.mapper.DelayedTaskDOMapper;
import io.micrometer.observation.ObservationRegistry;
import java.time.Clock;
import java.util.List;
import javax.sql.DataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@AutoConfiguration(
        afterName = {
            "com.mybatisflex.spring.boot.MybatisFlexAutoConfiguration",
            "io.github.loadup.components.database.autoconfigure.MyBatisFlexAutoConfiguration"
        })
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(prefix = "loadup.delayedtask", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DelayedTaskProperties.class)
@MapperScan(basePackageClasses = DelayedTaskDOMapper.class)
@Import(DelayedTaskConverterImpl.class)
public class DelayedTaskAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "delayedTaskClock")
    public Clock delayedTaskClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @ConditionalOnMissingBean
    public DelayedTaskStore delayedTaskStore(DelayedTaskDOMapper mapper) {
        return new DelayedTaskStore(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public DelayedTaskTemplate delayedTaskTemplate(
            DelayedTaskStore store,
            DelayedTaskConverter converter,
            DelayedTaskProperties properties,
            DatabaseProperties database,
            @Qualifier("delayedTaskClock") Clock clock,
            org.springframework.transaction.PlatformTransactionManager transactionManager) {
        return new DelayedTaskTemplate(store, converter, properties, database, clock, transactionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public DelayedTaskDispatcher delayedTaskDispatcher(
            DelayedTaskStore store,
            DelayedTaskConverter converter,
            DelayedTaskProperties props,
            @Qualifier("delayedTaskClock") Clock clock,
            org.springframework.beans.factory.ObjectProvider<ObservationRegistry> registry,
            List<DelayedTaskHandler> handlers) {
        return new DelayedTaskDispatcher(
                store, converter, props, clock, registry.getIfAvailable(() -> ObservationRegistry.NOOP), handlers);
    }
}
