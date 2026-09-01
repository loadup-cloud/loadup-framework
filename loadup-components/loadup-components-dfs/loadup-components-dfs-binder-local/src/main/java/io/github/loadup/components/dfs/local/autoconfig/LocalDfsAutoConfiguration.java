/*-
 * #%L
 * Loadup Dfs Binder Local
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
package io.github.loadup.components.dfs.local.autoconfig;

import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.autoconfig.DfsAutoConfiguration;
import io.github.loadup.components.dfs.local.LocalDfsProperties;
import io.github.loadup.components.dfs.local.LocalDfsProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Auto-configuration for the local filesystem binder. */
@AutoConfiguration(before = DfsAutoConfiguration.class)
@ConditionalOnProperty(prefix = "loadup.dfs", name = "binder-type", havingValue = "local", matchIfMissing = true)
@EnableConfigurationProperties(LocalDfsProperties.class)
public class LocalDfsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DfsProvider.class)
    public DfsProvider localDfsProvider(LocalDfsProperties properties) {
        return new LocalDfsProvider(properties);
    }
}
