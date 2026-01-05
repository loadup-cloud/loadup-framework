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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto configuration for DFS Database Binder
 *
 * <p>Enables MyBatis Mapper scanning for FileStorageEntityMapper
 *
 * <p>This binder is only loaded when ALL of the following conditions are met:
 *
 * <ul>
 *   <li>'loadup.dfs.default-provider=database'
 *   <li>DataSource bean exists (i.e., database is configured)
 * </ul>
 *
 * <p>By using @AutoConfigureAfter, this configuration is evaluated after
 * DataSourceAutoConfiguration, ensuring that @ConditionalOnBean(DataSource.class) works correctly.
 *
 * <p>This ensures that only the configured provider is loaded, avoiding configuration errors for
 * unused providers.
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.dfs", name = "default-provider", havingValue = "database")
@ComponentScan(basePackages = "com.github.loadup.components.dfs.binder.database")
@MapperScan("com.github.loadup.components.dfs.binder.database.mapper")
public class DfsDatabaseBinderAutoConfiguration {
  // MyBatis-Flex will automatically scan and register Mappers in the specified package
}
