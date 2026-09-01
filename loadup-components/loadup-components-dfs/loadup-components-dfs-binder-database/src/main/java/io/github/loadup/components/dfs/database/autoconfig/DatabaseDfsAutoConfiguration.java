/*-
 * #%L
 * Loadup Dfs Binder Database
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
package io.github.loadup.components.dfs.database.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loadup.components.database.autoconfig.MyBatisFlexAutoConfiguration;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.autoconfig.DfsAutoConfiguration;
import io.github.loadup.components.dfs.database.DatabaseDfsProvider;
import io.github.loadup.components.dfs.database.mapper.FileStorageMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/** Auto-configuration for the transitional database DFS binder. */
@AutoConfiguration(after = MyBatisFlexAutoConfiguration.class, before = DfsAutoConfiguration.class)
@ConditionalOnClass({FileStorageMapper.class, ObjectMapper.class})
@ConditionalOnProperty(prefix = "loadup.dfs", name = "binder-type", havingValue = "database")
@MapperScan("io.github.loadup.components.dfs.database.mapper")
public class DatabaseDfsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DfsProvider.class)
    public DfsProvider databaseDfsProvider(
            FileStorageMapper mapper, ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new DatabaseDfsProvider(mapper, objectMapperProvider.getIfAvailable(ObjectMapper::new));
    }
}
