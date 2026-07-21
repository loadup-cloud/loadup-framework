package io.github.loadup.gateway.plugins.config;

/*-
 * #%L
 * Repository Database Plugin
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.gateway.facade.spi.RepositoryPlugin;
import io.github.loadup.gateway.plugins.DatabaseRepositoryPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Database Repository Plugin.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.gateway.storage", name = "type", havingValue = "DATABASE")
public class DatabaseRepositoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "databaseRepositoryPlugin")
    public RepositoryPlugin databaseRepositoryPlugin() {
        DatabaseRepositoryPlugin plugin = new DatabaseRepositoryPlugin();
        // Note: DatabaseRepositoryPlugin uses @Resource for dependencies
        // Spring will inject them automatically
        plugin.initialize();
        log.info(">>> [GATEWAY] DatabaseRepositoryPlugin initialized");
        return plugin;
    }
}
