/*
 * #%L
 * LoadUp Outbox
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
package io.github.loadup.components.outbox;

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

/** Requires one selected DataSource; schema migration remains the application's responsibility. */
@AutoConfiguration(afterName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(prefix = "loadup.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public JdbcOutboxRepository outboxTemplate(DataSource source, OutboxProperties properties) {
        return new JdbcOutboxRepository(source, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxInbox outboxInbox(DataSource source) {
        return new OutboxInbox(source);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxDispatcher outboxDispatcher(
            JdbcOutboxRepository repository,
            OutboxProperties properties,
            List<OutboxHandler> handlers,
            ObjectProvider<MeterRegistry> metrics,
            ObjectProvider<ObservationRegistry> observations) {
        MeterRegistry registry = metrics.getIfAvailable();
        if (registry != null) {
            registry.gauge("loadup.outbox.pending", repository, JdbcOutboxRepository::pendingCount);
            registry.gauge("loadup.outbox.failed", repository, JdbcOutboxRepository::failedCount);
            registry.gauge("loadup.outbox.oldest.age", repository, JdbcOutboxRepository::oldestPendingSeconds);
        }
        return new OutboxDispatcher(
                repository,
                properties,
                handlers,
                registry,
                observations.getIfAvailable(() -> ObservationRegistry.NOOP));
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxWorker outboxWorker(OutboxDispatcher dispatcher, OutboxProperties properties) {
        return new OutboxWorker(dispatcher, properties);
    }
}
