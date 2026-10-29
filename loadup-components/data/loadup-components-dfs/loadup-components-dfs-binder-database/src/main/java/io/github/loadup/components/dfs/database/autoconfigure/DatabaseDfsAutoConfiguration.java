/*
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
package io.github.loadup.components.dfs.database.autoconfigure;

import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.database.tenant.DatabaseTenant;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import io.github.loadup.components.dfs.autoconfigure.DfsAutoConfiguration;
import io.github.loadup.components.dfs.database.DatabaseDfsProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tools.jackson.databind.ObjectMapper;

/** Auto-configuration for the transitional database DFS binder. */
@AutoConfiguration(before = DfsAutoConfiguration.class, afterName = {
        "io.github.loadup.components.database.autoconfigure.DatabaseCoreAutoConfiguration",
        "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration"})
@ConditionalOnClass({NamedParameterJdbcTemplate.class, ObjectMapper.class})
@ConditionalOnProperty(prefix = "loadup.dfs", name = "binder-type", havingValue = "database")
public class DatabaseDfsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DfsProvider.class)
    public DfsProvider databaseDfsProvider(
            NamedParameterJdbcTemplate jdbc, ObjectProvider<ObjectMapper> objectMapperProvider, DatabaseTenant tenant, @Qualifier("databaseClock") Clock clock) {
        return new DatabaseDfsProvider(jdbc, objectMapperProvider.getIfAvailable(ObjectMapper::new), tenant, clock);
    }
}
