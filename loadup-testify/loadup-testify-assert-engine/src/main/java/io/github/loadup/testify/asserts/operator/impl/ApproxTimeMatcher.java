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
import io.github.loadup.testify.asserts.operator.OperatorMatcher;
import io.github.loadup.testify.asserts.util.TimeParser;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * Approximate time matcher for handling time comparisons with delta tolerance. Supports multiple
 * timestamp formats: - java.time.Instant - java.time.LocalDateTime - java.sql.Timestamp - ISO-8601
 * strings - Epoch milliseconds (Long)
 */
public class ApproxTimeMatcher implements OperatorMatcher {
  private static final TimeParser timeParser = new TimeParser();

  @Override
  public boolean support(String op) {
    return "approx".equals(op);
  }

  @Override
  public MatchResult match(Object actual, Object val, Map<String, Object> config) {
    // 默认容差为 3000 毫秒
    long threshold = Long.parseLong(String.valueOf(config.getOrDefault("threshold", 3000)));

    try {
      long actTime = toMillis(actual);
      long expTime = toMillis(val);

      long diff = Math.abs(actTime - expTime);
      return diff <= threshold
          ? MatchResult.pass()
          : MatchResult.fail(
              actual, val, String.format("Time diff %dms exceeds threshold %dms", diff, threshold));
    } catch (Exception e) {
      return MatchResult.fail(actual, val, "Time parsing failed: " + e.getMessage());
    }
  }

  private long toMillis(Object value) {
    if (value == null) throw new IllegalArgumentException("时间值不能为空");

    // 如果是数字（时间戳）
    if (value instanceof Number n) return n.longValue();

    // 如果是 Java 8 时间对象
    if (value instanceof java.time.temporal.TemporalAccessor ta) {
      try {
        return java.time.Instant.from(ta).toEpochMilli();
      } catch (Exception e) {
        return LocalDateTime.from(ta).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
      }
    }

    // 如果是 SQL Timestamp 或 Date
    if (value instanceof java.util.Date d) return d.getTime();

    // 如果是字符串，尝试多种常用格式解析
    if (value instanceof String str) {
      return timeParser.toMillis(str);
    }

    throw new IllegalArgumentException("不支持的时间格式: " + value.getClass());
  }
}
