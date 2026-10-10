/*
 * #%L
 * LoadUp File App
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
package io.github.loadup.modules.file.app.autoconfigure;

import io.github.loadup.components.dfs.DfsService;
import io.github.loadup.modules.file.app.converter.FileDTOConverter;
import io.github.loadup.modules.file.app.converter.FileDTOConverterImpl;
import io.github.loadup.modules.file.app.service.FileResourceService;
import io.github.loadup.modules.file.domain.gateway.FileResourceGateway;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/** Creates file metadata services when both JDBC and a DFS binder are present. */
@Import(FileDTOConverterImpl.class)
@AutoConfiguration(
        afterName = {
            "io.github.loadup.modules.file.infrastructure.autoconfigure.FilePersistenceAutoConfiguration",
            "io.github.loadup.components.dfs.autoconfig.DfsAutoConfiguration"
        })
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnBean({FileResourceGateway.class, DfsService.class})
@ConditionalOnProperty(prefix = "loadup.modules.file", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileResourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(io.github.loadup.modules.file.client.facade.FileResourceFacade.class)
    public FileResourceService fileResourceService(
            FileResourceGateway repository, DfsService dfs, FileDTOConverter converter) {
        return new FileResourceService(repository, dfs, converter);
    }
}
