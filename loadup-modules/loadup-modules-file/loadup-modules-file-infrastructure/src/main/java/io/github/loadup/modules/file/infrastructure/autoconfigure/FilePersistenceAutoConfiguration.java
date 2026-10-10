/*
 * #%L
 * LoadUp File Infrastructure
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
package io.github.loadup.modules.file.infrastructure.autoconfigure;

import io.github.loadup.modules.file.domain.gateway.FileResourceGateway;
import io.github.loadup.modules.file.infrastructure.converter.FileStorageConverter;
import io.github.loadup.modules.file.infrastructure.converter.FileStorageConverterImpl;
import io.github.loadup.modules.file.infrastructure.mapper.*;
import io.github.loadup.modules.file.infrastructure.repository.FileResourceGatewayImpl;
import javax.sql.DataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;

@AutoConfiguration(afterName = "com.mybatisflex.spring.boot.MybatisFlexAutoConfiguration")
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(prefix = "loadup.modules.file", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FilePersistenceAutoConfiguration {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(FileResourceGateway.class)
    @MapperScan(basePackageClasses = FileResourceDOMapper.class)
    @Import(FileStorageConverterImpl.class)
    static class DefaultPersistence {
        @Bean
        public FileResourceGateway fileGateway(
                FileResourceDOMapper fileResourceMapper,
                FileReferenceDOMapper fileReferenceMapper,
                FileStorageConverter converter) {
            return new FileResourceGatewayImpl(fileResourceMapper, fileReferenceMapper, converter);
        }
    }
}
