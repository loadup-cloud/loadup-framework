package io.github.loadup.gateway.plugins.config;

/*-
 * #%L
 * Repository File Plugin
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

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.spi.RepositoryPlugin;
import io.github.loadup.gateway.plugins.FileRepositoryPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for File Repository Plugin.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.gateway.storage", name = "type", havingValue = "FILE", matchIfMissing = true)
public class FileRepositoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "fileRepositoryPlugin")
    public RepositoryPlugin fileRepositoryPlugin(GatewayProperties gatewayProperties) {
        FileRepositoryPlugin plugin = new FileRepositoryPlugin();
        // Inject GatewayProperties via reflection
        try {
            java.lang.reflect.Field field = FileRepositoryPlugin.class.getDeclaredField("gatewayProperties");
            field.setAccessible(true);
            field.set(plugin, gatewayProperties);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject GatewayProperties", e);
        }
        plugin.initialize();
        log.info(">>> [GATEWAY] FileRepositoryPlugin initialized");
        return plugin;
    }
}
