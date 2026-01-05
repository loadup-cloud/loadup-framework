package com.github.loadup.components.dfs.binder.database.config;

/*-
 * #%L
 * loadup-components-dfs-binder-database
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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
 * Auto configuration for DFS Database Binder
 *
 * <p>Enables MyBatis Mapper scanning for FileStorageEntityMapper
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.github.loadup.components.dfs.binder.database")
@MapperScan("com.github.loadup.components.dfs.binder.database.mapper")
public class DfsDatabaseBinderAutoConfiguration {
  // MyBatis-Flex will automatically scan and register Mappers in the specified package
}
