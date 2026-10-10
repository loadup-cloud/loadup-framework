/*
 * #%L
 * LoadUp Notification Infrastructure
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
package io.github.loadup.modules.notification.infrastructure.autoconfigure;

import io.github.loadup.modules.notification.domain.gateway.InboxGateway;
import io.github.loadup.modules.notification.infrastructure.converter.NotificationStorageConverter;
import io.github.loadup.modules.notification.infrastructure.converter.NotificationStorageConverterImpl;
import io.github.loadup.modules.notification.infrastructure.mapper.*;
import io.github.loadup.modules.notification.infrastructure.repository.InboxGatewayImpl;
import javax.sql.DataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;

@AutoConfiguration(afterName = "com.mybatisflex.spring.boot.MybatisFlexAutoConfiguration")
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(
        prefix = "loadup.modules.notification",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class NotificationPersistenceAutoConfiguration {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(InboxGateway.class)
    @MapperScan(basePackageClasses = InboxMessageDOMapper.class)
    @Import(NotificationStorageConverterImpl.class)
    static class DefaultPersistence {
        @Bean
        public InboxGateway notificationGateway(
                InboxMessageDOMapper inboxMessageMapper, NotificationStorageConverter converter) {
            return new InboxGatewayImpl(inboxMessageMapper, converter);
        }
    }
}
