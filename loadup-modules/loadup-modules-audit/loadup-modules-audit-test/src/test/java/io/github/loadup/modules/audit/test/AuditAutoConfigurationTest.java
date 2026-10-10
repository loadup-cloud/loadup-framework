/*
 * #%L
 * LoadUp Audit Test
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
package io.github.loadup.modules.audit.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.modules.audit.app.autoconfigure.AuditAutoConfiguration;
import io.github.loadup.modules.audit.app.converter.AuditDTOConverter;
import io.github.loadup.modules.audit.app.service.AuditService;
import io.github.loadup.modules.audit.domain.gateway.AuditGateway;
import io.github.loadup.modules.audit.infrastructure.autoconfigure.AuditPersistenceAutoConfiguration;
import io.github.loadup.modules.audit.infrastructure.mapper.AuditEventDOMapper;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** Exercises auto-configuration composition; no persistence operations are executed. */
class AuditAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(DataSource.class, () -> mock(DataSource.class))
            .withBean("auditEventDOMapper", AuditEventDOMapper.class, () -> mock(AuditEventDOMapper.class))
            .withConfiguration(
                    AutoConfigurations.of(AuditPersistenceAutoConfiguration.class, AuditAutoConfiguration.class));

    @Test
    void wiresDomainPortAndApplicationMapper() {
        runner.run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(AuditGateway.class)
                    .hasSingleBean(AuditDTOConverter.class)
                    .hasSingleBean(AuditService.class);
        });
    }

    @Test
    void consumerGatewayOverridesDefaultAdapter() {
        AuditGateway gateway = mock(AuditGateway.class);
        runner.withBean(AuditGateway.class, () -> gateway).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(AuditGateway.class).hasSingleBean(AuditService.class);
            assertThat(context.getBean(AuditGateway.class)).isSameAs(gateway);
        });
    }

    @Test
    void respectsDisabledFeature() {
        runner.withPropertyValues("loadup.modules.audit.enabled=false").run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .doesNotHaveBean(AuditGateway.class)
                    .doesNotHaveBean(AuditService.class);
        });
    }
}
