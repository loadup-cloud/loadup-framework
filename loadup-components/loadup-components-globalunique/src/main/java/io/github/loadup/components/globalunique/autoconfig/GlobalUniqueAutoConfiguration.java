/*-
 * #%L
 * LoadUp Components :: Global Unique
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

package io.github.loadup.components.globalunique.autoconfig;

import io.github.loadup.components.database.autoconfig.MyBatisFlexAutoConfiguration;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.globalunique.DefaultGlobalUniqueTemplate;
import io.github.loadup.components.globalunique.GlobalUniqueProperties;
import io.github.loadup.components.globalunique.GlobalUniqueTemplate;
import io.github.loadup.components.globalunique.mapper.GlobalUniqueMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Configures the database-backed global unique template. */
@AutoConfiguration(after = MyBatisFlexAutoConfiguration.class)
@ConditionalOnProperty(prefix = "loadup.global-unique", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GlobalUniqueProperties.class)
public class GlobalUniqueAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(GlobalUniqueTemplate.class)
    public GlobalUniqueTemplate globalUniqueTemplate(GlobalUniqueMapper mapper, DatabaseProperties databaseProperties) {
        return new DefaultGlobalUniqueTemplate(mapper, databaseProperties);
    }
}
