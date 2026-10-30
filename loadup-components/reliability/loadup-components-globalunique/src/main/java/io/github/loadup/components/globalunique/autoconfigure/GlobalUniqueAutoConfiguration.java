/*
 * #%L
 * LoadUp Components Global Unique
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
package io.github.loadup.components.globalunique.autoconfigure;

import io.github.loadup.components.database.autoconfigure.DatabaseCoreAutoConfiguration;
import io.github.loadup.components.database.autoconfigure.IdGeneratorAutoConfiguration;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.components.globalunique.DefaultGlobalUniqueTemplate;
import io.github.loadup.components.globalunique.GlobalUniqueProperties;
import io.github.loadup.components.globalunique.GlobalUniqueTemplate;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Configures the database-backed global unique template.
 */
@AutoConfiguration(
        after = {IdGeneratorAutoConfiguration.class, DatabaseCoreAutoConfiguration.class},
        afterName = "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration")
@ConditionalOnBean({JdbcTemplate.class, IdGenerator.class})
@ConditionalOnProperty(prefix = "loadup.global-unique", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GlobalUniqueProperties.class)
public class GlobalUniqueAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(GlobalUniqueTemplate.class)
    public GlobalUniqueTemplate globalUniqueTemplate(JdbcTemplate jdbc, IdGenerator idGenerator, Clock clock) {
        return new DefaultGlobalUniqueTemplate(jdbc, idGenerator, clock);
    }
}
