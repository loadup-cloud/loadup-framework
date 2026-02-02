package io.github.loadup.modules.upms.starter;

/*-
 * #%L
 * Loadup Modules UPMS Starter
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

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * UPMS Auto Configuration
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@AutoConfiguration
@EnableAsync
@EnableConfigurationProperties
// @EnableJdbcRepositories(basePackages =
// "io.github.loadup.modules.upms.infrastructure.repository")
@MapperScan("io.github.loadup.modules.upms.infrastructure.mapper")
@ComponentScan(
        basePackages = {
            "io.github.loadup.modules.upms.domain",
            "io.github.loadup.modules.upms.infrastructure",
            "io.github.loadup.modules.upms.app",
            "io.github.loadup.modules.upms.security",
            "io.github.loadup.modules.upms.adapter",
            "io.github.loadup.modules.upms.app.service"
        })
public class UpmsAutoConfiguration {
    // Auto-configuration for UPMS module
}
