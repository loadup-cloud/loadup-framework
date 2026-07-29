package io.github.loadup.components.dfs.local.autoconfig;

/*-
 * #%L
 * Loadup Dfs Binder Local
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.local.LocalDfsConfig;
import io.github.loadup.components.dfs.local.LocalDfsProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.dfs", name = "binder-type", havingValue = "local", matchIfMissing = true)
@EnableConfigurationProperties(LocalDfsConfig.class)
public class LocalDfsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public DfsProvider localDfsProvider(LocalDfsConfig config) {
        return new LocalDfsProvider(config);
    }
}
