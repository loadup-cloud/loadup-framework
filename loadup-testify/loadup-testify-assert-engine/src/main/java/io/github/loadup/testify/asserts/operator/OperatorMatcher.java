package io.github.loadup.testify.asserts.operator;

/*-
 * #%L
 * Testify Assert Engine
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

import io.github.loadup.testify.asserts.model.MatchResult;
import java.util.Map;

public interface OperatorMatcher {
  /**
   * @param actual 数据库查询到的实际值
   * @param expectedValue YAML中配置的期望对象 (如 {op: "gt", val: 100})
   * @return 匹配结果描述，如果成功返回 null，失败返回错误原因
   */
  MatchResult match(Object actual, Object expectedValue, Map<String, Object> config);

  boolean support(String op);
}
