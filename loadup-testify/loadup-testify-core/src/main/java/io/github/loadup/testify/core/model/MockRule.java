package io.github.loadup.testify.core.model;

/*-
 * #%L
 * Testify Core
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

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MockRule {
  private List<Object> expectedArgs; // 预期参数条件
  private Object returnValue; // 原始返回配置
  private Throwable throwEx; // 异常配置
  private Class<?> returnType; // 方法返回类型
  private Map<String, Object> context; // 变量上下文
  private Long delay; // 新增存储
  private boolean hit; // 记录是否被触发过
}
