/*
 * #%L
 * LoadUp Dictionary Infrastructure
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
package io.github.loadup.modules.dictionary.infrastructure.autoconfigure;

import io.github.loadup.modules.dictionary.domain.gateway.DictionaryGateway;
import io.github.loadup.modules.dictionary.infrastructure.converter.DictionaryStorageConverter;
import io.github.loadup.modules.dictionary.infrastructure.converter.DictionaryStorageConverterImpl;
import io.github.loadup.modules.dictionary.infrastructure.mapper.*;
import io.github.loadup.modules.dictionary.infrastructure.repository.DictionaryGatewayImpl;
import javax.sql.DataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;

@AutoConfiguration(afterName = "com.mybatisflex.spring.boot.MybatisFlexAutoConfiguration")
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(
        prefix = "loadup.modules.dictionary",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DictionaryPersistenceAutoConfiguration {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(DictionaryGateway.class)
    @MapperScan(basePackageClasses = DictionaryTypeDOMapper.class)
    @Import(DictionaryStorageConverterImpl.class)
    static class DefaultPersistence {
        @Bean
        public DictionaryGateway dictionaryGateway(
                DictionaryTypeDOMapper dictionaryTypeMapper,
                DictionaryItemDOMapper dictionaryItemMapper,
                DictionaryStorageConverter converter) {
            return new DictionaryGatewayImpl(dictionaryTypeMapper, dictionaryItemMapper, converter);
        }
    }
}
