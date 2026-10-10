/*
 * #%L
 * LoadUp Transfer Test
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
package io.github.loadup.modules.transfer.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.loadup.modules.file.app.service.FileResourceService;
import io.github.loadup.modules.transfer.app.autoconfigure.TransferAutoConfiguration;
import io.github.loadup.modules.transfer.app.config.TransferTaskProperties;
import io.github.loadup.modules.transfer.app.converter.TransferDTOConverter;
import io.github.loadup.modules.transfer.app.service.TransferTaskService;
import io.github.loadup.modules.transfer.client.enums.TransferKind;
import io.github.loadup.modules.transfer.client.spi.TransferHandler;
import io.github.loadup.modules.transfer.domain.gateway.TransferGateway;
import io.github.loadup.modules.transfer.infrastructure.autoconfigure.TransferPersistenceAutoConfiguration;
import io.github.loadup.modules.transfer.infrastructure.mapper.TransferTaskDOMapper;
import io.github.loadup.modules.transfer.infrastructure.mapper.TransferTaskOptionDOMapper;
import io.github.loadup.retrytask.facade.RetryTaskFacade;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** Exercises auto-configuration composition; no persistence operations are executed. */
class TransferAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(DataSource.class, () -> mock(DataSource.class))
            .withBean("transferTaskDOMapper", TransferTaskDOMapper.class, () -> mock(TransferTaskDOMapper.class))
            .withBean(
                    "transferTaskOptionDOMapper",
                    TransferTaskOptionDOMapper.class,
                    () -> mock(TransferTaskOptionDOMapper.class))
            .withBean(FileResourceService.class, () -> mock(FileResourceService.class))
            .withBean(RetryTaskFacade.class, () -> mock(RetryTaskFacade.class))
            .withBean(TransferHandler.class, () -> {
                TransferHandler handler = mock(TransferHandler.class);
                when(handler.kind()).thenReturn(TransferKind.EXPORT);
                when(handler.key()).thenReturn("test-export");
                return handler;
            })
            .withConfiguration(
                    AutoConfigurations.of(TransferPersistenceAutoConfiguration.class, TransferAutoConfiguration.class));

    @Test
    void wiresDomainPortAndApplicationMapper() {
        runner.run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(TransferGateway.class)
                    .hasSingleBean(TransferDTOConverter.class)
                    .hasSingleBean(TransferTaskService.class);
        });
    }

    @Test
    void consumerGatewayOverridesDefaultAdapter() {
        TransferGateway gateway = mock(TransferGateway.class);
        runner.withBean(TransferGateway.class, () -> gateway).run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(TransferGateway.class)
                    .hasSingleBean(TransferTaskService.class);
            assertThat(context.getBean(TransferGateway.class)).isSameAs(gateway);
        });
    }

    @Test
    void bindsTransferLimitsInModulesNamespace() {
        runner.withPropertyValues(
                        "loadup.modules.transfer.max-input-bytes=4096", "loadup.modules.transfer.max-output-bytes=8192")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    TransferTaskProperties properties = context.getBean(TransferTaskProperties.class);
                    assertThat(properties.getMaxInputBytes()).isEqualTo(4096);
                    assertThat(properties.getMaxOutputBytes()).isEqualTo(8192);
                });
    }

    @Test
    void respectsDisabledFeature() {
        runner.withPropertyValues("loadup.modules.transfer.enabled=false").run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .doesNotHaveBean(TransferGateway.class)
                    .doesNotHaveBean(TransferTaskService.class);
        });
    }
}
