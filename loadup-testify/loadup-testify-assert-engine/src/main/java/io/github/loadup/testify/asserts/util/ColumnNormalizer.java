package io.github.loadup.testify.asserts.util;

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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility for normalizing database column names to support case-insensitive and
 * underscore/camelCase matching.
 */
public class ColumnNormalizer {

  /**
   * Normalize a map of database results to use case-insensitive keys. All keys are converted to
   * lowercase.
   */
  public static Map<String, Object> normalizeCaseInsensitive(Map<String, Object> row) {
    if (row == null) {
      return null;
    }

    Map<String, Object> normalized = new HashMap<>();
    row.forEach(
        (key, value) -> {
          String normalizedKey = key != null ? key.toLowerCase(Locale.ROOT) : null;
          normalized.put(normalizedKey, value);
        });

    return normalized;
  }

  /** Convert snake_case to camelCase. Example: user_name -> userName */
  public static String snakeToCamel(String snakeCase) {
    if (snakeCase == null || !snakeCase.contains("_")) {
      return snakeCase;
    }

    StringBuilder camelCase = new StringBuilder();
    boolean capitalizeNext = false;

    for (char c : snakeCase.toCharArray()) {
      if (c == '_') {
        capitalizeNext = true;
      } else {
        if (capitalizeNext) {
          camelCase.append(Character.toUpperCase(c));
          capitalizeNext = false;
        } else {
          camelCase.append(c);
        }
      }
    }

    return camelCase.toString();
  }

  /** Convert camelCase to snake_case. Example: userName -> user_name */
  public static String camelToSnake(String camelCase) {
    if (camelCase == null) {
      return null;
    }

    StringBuilder snakeCase = new StringBuilder();

    for (int i = 0; i < camelCase.length(); i++) {
      char c = camelCase.charAt(i);
      if (Character.isUpperCase(c) && i > 0) {
        snakeCase.append('_');
        snakeCase.append(Character.toLowerCase(c));
      } else {
        snakeCase.append(Character.toLowerCase(c));
      }
    }

    return snakeCase.toString();
  }

  /**
   * Normalize column name based on strategy.
   *
   * @param columnName Original column name
   * @param strategy "camelCase", "snake_case", or "caseInsensitive"
   * @return Normalized column name
   */
  public static String normalize(String columnName, String strategy) {
    if (columnName == null) {
      return null;
    }

    return switch (strategy) {
      case "camelCase" -> snakeToCamel(columnName);
      case "snake_case" -> camelToSnake(columnName);
      case "caseInsensitive" -> columnName.toLowerCase(Locale.ROOT);
      default -> columnName;
    };
  }

  /** Normalize all keys in a map based on strategy. */
  public static Map<String, Object> normalizeMap(Map<String, Object> map, String strategy) {
    if (map == null) {
      return null;
    }

    Map<String, Object> normalized = new HashMap<>();
    map.forEach(
        (key, value) -> {
          String normalizedKey = normalize(key, strategy);
          normalized.put(normalizedKey, value);
        });

    return normalized;
  }
}
