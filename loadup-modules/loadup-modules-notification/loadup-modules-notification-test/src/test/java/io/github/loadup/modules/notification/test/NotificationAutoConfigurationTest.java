/*
 * #%L
 * LoadUp Notification Test
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
package io.github.loadup.modules.notification.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.modules.notification.app.autoconfigure.NotificationAutoConfiguration;
import io.github.loadup.modules.notification.app.converter.NotificationDTOConverter;
import io.github.loadup.modules.notification.app.service.InboxService;
import io.github.loadup.modules.notification.domain.gateway.InboxGateway;
import io.github.loadup.modules.notification.infrastructure.autoconfigure.NotificationPersistenceAutoConfiguration;
import io.github.loadup.modules.notification.infrastructure.mapper.InboxMessageMapper;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** Exercises auto-configuration composition; no persistence operations are executed. */
class NotificationAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(DataSource.class, () -> mock(DataSource.class))
            .withBean("inboxMessageMapper", InboxMessageMapper.class, () -> mock(InboxMessageMapper.class))
            .withConfiguration(AutoConfigurations.of(
                    NotificationPersistenceAutoConfiguration.class, NotificationAutoConfiguration.class));

    @Test
    void wiresDomainPortAndApplicationMapper() {
        runner.run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(InboxGateway.class)
                    .hasSingleBean(NotificationDTOConverter.class)
                    .hasSingleBean(InboxService.class);
        });
    }

    @Test
    void consumerGatewayOverridesDefaultAdapter() {
        InboxGateway gateway = mock(InboxGateway.class);
        runner.withBean(InboxGateway.class, () -> gateway).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(InboxGateway.class).hasSingleBean(InboxService.class);
            assertThat(context.getBean(InboxGateway.class)).isSameAs(gateway);
        });
    }

    @Test
    void respectsDisabledFeature() {
        runner.withPropertyValues("loadup.modules.notification.enabled=false").run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .doesNotHaveBean(InboxGateway.class)
                    .doesNotHaveBean(InboxService.class);
        });
    }
}
