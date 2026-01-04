package com.github.loadup.components.database.autoconfig;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import com.github.loadup.components.database.config.DatabaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MyBatis-Flex Auto Configuration
 *
 * <p>MyBatis-Flex 基础配置，后续可扩展多租户、逻辑删除等功能
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(DatabaseProperties.class)
@RequiredArgsConstructor
public class MyBatisFlexAutoConfiguration {

  private final DatabaseProperties databaseProperties;

  // TODO: 配置多租户拦截器
  // TODO: 配置实体监听器
  // TODO: 配置逻辑删除

  // MyBatis-Flex 会自动扫描 Mapper 接口并配置
}
