package io.github.loadup.testify.infra.container;

/*-
 * #%L
 * Testify Infra Common
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

import java.util.Map;

public interface InfraProvider {
  /** 判断当前 Provider 支持的服务类型 (mysql, kafka, etc.) */
  String getType();

  /** 启动容器实例 */
  void start(Map<String, Object> config, boolean reuse);

  /** 获取容器启动后的连接属性映射 (如 spring.datasource.url -> jdbc:mysql://...) */
  Map<String, String> getExposedProperties();

  /** 停止容器 */
  void stop();
}
