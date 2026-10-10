/*
 * #%L
 * LoadUp Notification App
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
package io.github.loadup.modules.notification.app.autoconfigure;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.modules.notification.app.converter.NotificationDTOConverter;
import io.github.loadup.modules.notification.app.converter.NotificationDTOConverterImpl;
import io.github.loadup.modules.notification.app.integration.InAppChannelProvider;
import io.github.loadup.modules.notification.app.service.InboxService;
import io.github.loadup.modules.notification.domain.gateway.InboxGateway;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import(NotificationDTOConverterImpl.class)
@AutoConfiguration(
        afterName =
                "io.github.loadup.modules.notification.infrastructure.autoconfigure.NotificationPersistenceAutoConfiguration",
        beforeName = "io.github.loadup.components.gotone.engine.GotoneEngineAutoConfiguration")
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnBean(InboxGateway.class)
@ConditionalOnProperty(
        prefix = "loadup.modules.notification",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class NotificationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(io.github.loadup.modules.notification.client.facade.InboxFacade.class)
    public InboxService inboxService(InboxGateway repository, NotificationDTOConverter converter) {
        return new InboxService(repository, converter);
    }

    @Bean
    public NotificationChannelProvider inAppNotificationChannelProvider(InboxService inbox) {
        return new InAppChannelProvider(inbox);
    }
}
