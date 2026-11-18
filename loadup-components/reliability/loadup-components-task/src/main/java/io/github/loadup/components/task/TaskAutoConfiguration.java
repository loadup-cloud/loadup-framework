/*
 * Copyright (C) 2026 LoadUp Cloud
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
package io.github.loadup.components.task;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

@AutoConfiguration(
        afterName = {
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
            "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration",
            "io.github.loadup.components.database.autoconfigure.JdbcTransactionAutoConfiguration"
        })
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(prefix = "loadup.task", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TaskProperties.class)
public class TaskAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public JdbcTaskRepository taskTemplate(
            DataSource source, PlatformTransactionManager manager, TaskProperties properties) {
        return new JdbcTaskRepository(source, manager, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskDispatcher taskDispatcher(
            JdbcTaskRepository repository,
            TaskProperties properties,
            List<TaskHandler> handlers,
            ObjectProvider<MeterRegistry> metrics,
            ObjectProvider<ObservationRegistry> observations) {
        MeterRegistry registry = metrics.getIfAvailable();
        if (registry != null) {
            registry.gauge("loadup.task.pending", repository, JdbcTaskRepository::pendingCount);
            registry.gauge("loadup.task.failed", repository, JdbcTaskRepository::failedCount);
            registry.gauge("loadup.task.oldest.age", repository, JdbcTaskRepository::oldestPendingSeconds);
        }
        return new TaskDispatcher(
                repository,
                properties,
                handlers,
                registry,
                observations.getIfAvailable(() -> ObservationRegistry.NOOP));
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskWorker taskWorker(TaskDispatcher dispatcher, TaskProperties properties) {
        return new TaskWorker(dispatcher, properties);
    }
}
