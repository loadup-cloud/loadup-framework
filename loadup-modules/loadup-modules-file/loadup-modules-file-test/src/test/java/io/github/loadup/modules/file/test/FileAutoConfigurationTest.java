/*
 * #%L
 * LoadUp File Test
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
package io.github.loadup.modules.file.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.components.dfs.DfsService;
import io.github.loadup.modules.file.app.autoconfigure.FileResourceAutoConfiguration;
import io.github.loadup.modules.file.app.converter.FileDTOConverter;
import io.github.loadup.modules.file.app.service.FileResourceService;
import io.github.loadup.modules.file.domain.gateway.FileResourceGateway;
import io.github.loadup.modules.file.infrastructure.autoconfigure.FilePersistenceAutoConfiguration;
import io.github.loadup.modules.file.infrastructure.mapper.FileReferenceMapper;
import io.github.loadup.modules.file.infrastructure.mapper.FileResourceMapper;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** Exercises auto-configuration composition; no persistence operations are executed. */
class FileAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(DataSource.class, () -> mock(DataSource.class))
            .withBean("fileResourceMapper", FileResourceMapper.class, () -> mock(FileResourceMapper.class))
            .withBean("fileReferenceMapper", FileReferenceMapper.class, () -> mock(FileReferenceMapper.class))
            .withBean(DfsService.class, () -> mock(DfsService.class))
            .withConfiguration(
                    AutoConfigurations.of(FilePersistenceAutoConfiguration.class, FileResourceAutoConfiguration.class));

    @Test
    void wiresDomainPortAndApplicationMapper() {
        runner.run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(FileResourceGateway.class)
                    .hasSingleBean(FileDTOConverter.class)
                    .hasSingleBean(FileResourceService.class);
        });
    }

    @Test
    void consumerGatewayOverridesDefaultAdapter() {
        FileResourceGateway gateway = mock(FileResourceGateway.class);
        runner.withBean(FileResourceGateway.class, () -> gateway).run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(FileResourceGateway.class)
                    .hasSingleBean(FileResourceService.class);
            assertThat(context.getBean(FileResourceGateway.class)).isSameAs(gateway);
        });
    }

    @Test
    void respectsDisabledFeature() {
        runner.withPropertyValues("loadup.modules.file.enabled=false").run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .doesNotHaveBean(FileResourceGateway.class)
                    .doesNotHaveBean(FileResourceService.class);
        });
    }
}
