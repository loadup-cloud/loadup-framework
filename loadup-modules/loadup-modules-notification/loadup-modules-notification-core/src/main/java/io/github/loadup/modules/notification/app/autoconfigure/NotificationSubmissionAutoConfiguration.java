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
package io.github.loadup.modules.notification.app.autoconfigure;

import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.components.outbox.OutboxAutoConfiguration;
import io.github.loadup.components.outbox.OutboxTemplate;
import io.github.loadup.modules.notification.app.converter.NotificationSubmissionConverter;
import io.github.loadup.modules.notification.app.converter.NotificationSubmissionConverterImpl;
import io.github.loadup.modules.notification.app.service.InAppDeliveryHandler;
import io.github.loadup.modules.notification.app.service.NotificationSubmissionService;
import io.github.loadup.modules.notification.client.facade.InboxFacade;
import io.github.loadup.modules.notification.client.facade.NotificationFacade;
import io.github.loadup.modules.notification.domain.gateway.NotificationSubmissionGateway;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

@AutoConfiguration(after = {NotificationAutoConfiguration.class, OutboxAutoConfiguration.class})
@ConditionalOnBean({NotificationSubmissionGateway.class, OutboxTemplate.class, InboxFacade.class})
@ConditionalOnProperty(
        prefix = "loadup.modules.notification",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@Import(NotificationSubmissionConverterImpl.class)
public class NotificationSubmissionAutoConfiguration {
    @Bean(defaultCandidate = false)
    @ConditionalOnMissingBean(name = "notificationClock")
    public Clock notificationClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @ConditionalOnMissingBean(NotificationFacade.class)
    public NotificationSubmissionService notificationSubmissionService(
            NotificationSubmissionGateway gateway,
            OutboxTemplate outbox,
            IdGenerator ids,
            JsonMapper json,
            NotificationSubmissionConverter converter,
            @Qualifier("notificationClock") Clock clock) {
        return new NotificationSubmissionService(gateway, outbox, ids, json, converter, clock);
    }

    @Bean
    @ConditionalOnMissingBean(InAppDeliveryHandler.class)
    public InAppDeliveryHandler inAppDeliveryHandler(
            NotificationSubmissionGateway gateway,
            InboxFacade inbox,
            JsonMapper json,
            @Qualifier("notificationClock") Clock clock) {
        return new InAppDeliveryHandler(gateway, inbox, json, clock);
    }
}
