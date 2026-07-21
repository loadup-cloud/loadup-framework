package io.github.loadup.modules.config.app.autoconfigure;

/*-
 * #%L
 * Loadup Modules Config App
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
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration entry point for the Config module.
 *
 * <p>Registers all beans in the config module layers via component scan and
 * enables MyBatis-Flex mapper scanning.
 *
 * @author LoadUp Framework
 */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.loadup.modules.config")
@MapperScan("io.github.loadup.modules.config.infrastructure.mapper")
public class ConfigModuleAutoConfiguration {}
