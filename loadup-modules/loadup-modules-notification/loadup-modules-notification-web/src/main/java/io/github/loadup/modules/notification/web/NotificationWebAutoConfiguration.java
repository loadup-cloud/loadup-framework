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
package io.github.loadup.modules.notification.web;

import io.github.loadup.modules.notification.client.facade.InboxFacade;
import io.github.loadup.modules.notification.client.facade.NotificationFacade;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import(NotificationWebConverterImpl.class)
@AutoConfiguration(
        afterName = {
            "io.github.loadup.modules.notification.app.autoconfigure.NotificationAutoConfiguration",
            "io.github.loadup.modules.notification.app.autoconfigure.NotificationSubmissionAutoConfiguration"
        })
@ConditionalOnBean(InboxFacade.class)
public class NotificationWebAutoConfiguration {
    @Bean
    @ConditionalOnBean(NotificationFacade.class)
    public NotificationSubmissionController notificationSubmissionController(NotificationFacade notifications) {
        return new NotificationSubmissionController(notifications);
    }

    @Bean
    public NotificationController notificationController(InboxFacade inbox, NotificationWebConverter converter) {
        return new NotificationController(inbox, converter);
    }
}
