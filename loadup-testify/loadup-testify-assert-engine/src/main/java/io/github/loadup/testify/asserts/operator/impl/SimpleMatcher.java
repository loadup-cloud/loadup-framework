package io.github.loadup.testify.asserts.operator.impl;

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
import java.math.BigDecimal;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/** Simple equality and inequality matcher. Handles null values properly. */
public class SimpleMatcher {
  public static MatchResult eq(Object actual, Object expected) {
    if (actual == null && expected == null) return MatchResult.pass();
    if (actual == null || expected == null)
      return MatchResult.fail(actual, expected, "One of the values is null");

    // 核心：处理数字和字符串的等值比对（如 Long 123 vs Integer 123）
    if (StringUtils.isNumeric(actual.toString()) && StringUtils.isNumeric(expected.toString())) {
      return new BigDecimal(actual.toString()).compareTo(new BigDecimal(expected.toString())) == 0
          ? MatchResult.pass()
          : MatchResult.fail(actual, expected, "Numeric values not equal");
    }

    return Objects.equals(actual, expected)
        ? MatchResult.pass()
        : MatchResult.fail(actual, expected, "Values are not equal");
  }
}
