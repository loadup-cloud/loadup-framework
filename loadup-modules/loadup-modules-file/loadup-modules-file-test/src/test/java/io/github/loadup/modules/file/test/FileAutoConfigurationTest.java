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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.dfs.DfsService;
import io.github.loadup.modules.file.app.autoconfigure.FileResourceAutoConfiguration;
import io.github.loadup.modules.file.app.converter.FileDTOConverter;
import io.github.loadup.modules.file.app.service.FileResourceService;
import io.github.loadup.modules.file.domain.gateway.FileResourceGateway;
import io.github.loadup.modules.file.domain.model.FileResource;
import io.github.loadup.modules.file.infrastructure.autoconfigure.FilePersistenceAutoConfiguration;
import io.github.loadup.modules.file.infrastructure.dataobject.FileResourceInsert;
import io.github.loadup.modules.file.infrastructure.mapper.FileReferenceRowMapper;
import io.github.loadup.modules.file.infrastructure.mapper.FileResourceInsertMapper;
import io.github.loadup.modules.file.infrastructure.repository.FileResourceGatewayImpl;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Exercises auto-configuration composition; no persistence operations are executed.
 */
class FileAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(DataSource.class, () -> mock(DataSource.class))
            .withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
            .withBean(FileResourceInsertMapper.class, () -> mock(FileResourceInsertMapper.class))
            .withBean(FileReferenceRowMapper.class, () -> mock(FileReferenceRowMapper.class))
            .withBean("databaseClock", Clock.class, Clock::systemDefaultZone)
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

    @Test
    void downloadUsesCurrentStorageTenantAndRestoresCallerContext() {
        var gateway = mock(FileResourceGateway.class);
        var dfs = mock(DfsService.class);
        var now = LocalDateTime.of(2026, 1, 1, 0, 0);
        var file =
                new FileResource("file", "owner", "storage", "name", "text/plain", 0, "database", "ACTIVE", now, now);
        when(gateway.find("file")).thenReturn(Optional.of(file));
        when(dfs.download("storage")).thenAnswer(invocation -> {
            assertThat(TenantUtil.getTenantId()).isEqualTo("outer-tenant");
            return null;
        });
        var service = new FileResourceService(gateway, dfs, mock(FileDTOConverter.class), Clock.systemUTC());
        TenantUtil.runWithTenant("outer-tenant", () -> {
            service.download("file", "owner", false);
            assertThat(TenantUtil.getTenantId()).isEqualTo("outer-tenant");
        });
    }

    @Test
    void fileAggregatePreservesAssignedIdAndReadMapping() {
        var resources = mock(FileResourceInsertMapper.class);
        var references = mock(FileReferenceRowMapper.class);
        var gateway = new FileResourceGatewayImpl(mock(JdbcTemplate.class), resources, references, Clock.systemUTC());
        var now = LocalDateTime.of(2026, 1, 1, 0, 0);
        var file =
                new FileResource("file", "owner", "storage", "name", "text/plain", 7, "database", "ACTIVE", now, now);
        gateway.insert(file);
        var inserted = ArgumentCaptor.forClass(FileResourceInsert.class);
        verify(resources).insert(inserted.capture(), org.mockito.ArgumentMatchers.eq(true));
        assertThat(inserted.getValue().getId()).isEqualTo("file");
        assertThat(inserted.getValue().getDeleted()).isFalse();
        when(resources.selectOneById("file")).thenReturn(inserted.getValue());
        var read = gateway.find("file").orElseThrow();
        assertThat(read.id()).isEqualTo(file.id());
        assertThat(read.storageId()).isEqualTo(file.storageId());
        assertThat(read.size()).isEqualTo(file.size());
        assertThat(read.state()).isEqualTo(file.state());
    }
}
