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

import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.modules.dictionary.domain.gateway.DictionaryGateway;
import io.github.loadup.modules.dictionary.infrastructure.converter.DictionaryStorageConverter;
import io.github.loadup.modules.dictionary.infrastructure.converter.DictionaryStorageConverterImpl;
import io.github.loadup.modules.dictionary.infrastructure.mapper.DictionaryItemMapper;
import io.github.loadup.modules.dictionary.infrastructure.mapper.DictionaryTypeMapper;
import io.github.loadup.modules.dictionary.infrastructure.repository.DictionaryGatewayImpl;
import java.time.Clock;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@AutoConfiguration(
        afterName = {
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
            "com.mybatisflex.spring.boot.v4.MybatisFlexAutoConfiguration",
            "io.github.loadup.components.database.autoconfigure.DatabaseCoreAutoConfiguration",
            "io.github.loadup.components.database.autoconfigure.IdGeneratorAutoConfiguration"
        })
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(
        prefix = "loadup.modules.dictionary",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DictionaryPersistenceAutoConfiguration {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(DictionaryGateway.class)
    @Import(DictionaryStorageConverterImpl.class)
    static class DefaultPersistence {
        @Bean
        public DictionaryGateway dictionaryGateway(
                NamedParameterJdbcTemplate jdbc,
                DictionaryTypeMapper typeMapper,
                DictionaryItemMapper itemMapper,
                IdGenerator ids,
                DictionaryStorageConverter converter,
                Clock clock) {
            return new DictionaryGatewayImpl(jdbc, typeMapper, itemMapper, ids, converter, clock);
        }
    }
}
