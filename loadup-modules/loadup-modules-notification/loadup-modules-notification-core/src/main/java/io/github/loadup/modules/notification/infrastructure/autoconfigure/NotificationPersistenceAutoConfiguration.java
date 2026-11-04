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
package io.github.loadup.modules.notification.infrastructure.autoconfigure;

import io.github.loadup.components.database.autoconfigure.DatabaseCoreAutoConfiguration;
import io.github.loadup.components.database.autoconfigure.IdGeneratorAutoConfiguration;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.modules.notification.domain.gateway.InboxGateway;
import io.github.loadup.modules.notification.domain.gateway.NotificationSubmissionGateway;
import io.github.loadup.modules.notification.infrastructure.converter.NotificationStorageConverter;
import io.github.loadup.modules.notification.infrastructure.converter.NotificationStorageConverterImpl;
import io.github.loadup.modules.notification.infrastructure.repository.InboxGatewayImpl;
import io.github.loadup.modules.notification.infrastructure.repository.JdbcNotificationSubmissionGateway;
import java.time.Clock;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@AutoConfiguration(
        after = {
            DataSourceAutoConfiguration.class,
            DatabaseCoreAutoConfiguration.class,
            IdGeneratorAutoConfiguration.class
        })
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(
        prefix = "loadup.modules.notification",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class NotificationPersistenceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(NotificationSubmissionGateway.class)
    public NotificationSubmissionGateway notificationSubmissionGateway(DataSource dataSource) {
        return new JdbcNotificationSubmissionGateway(JdbcClient.create(dataSource));
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(InboxGateway.class)
    @Import(NotificationStorageConverterImpl.class)
    static class DefaultPersistence {
        @Bean
        public InboxGateway notificationGateway(
                ObjectProvider<JdbcClient> jdbcClient,
                DataSource dataSource,
                IdGenerator ids,
                NotificationStorageConverter converter,
                Clock clock) {
            return new InboxGatewayImpl(
                    jdbcClient.getIfAvailable(() -> JdbcClient.create(dataSource)), converter, clock, ids);
        }
    }
}
