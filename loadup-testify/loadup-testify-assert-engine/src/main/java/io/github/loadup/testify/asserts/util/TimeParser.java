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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

public class TimeParser {

  // 预定义的常用格式列表（基于实际业务常见格式）
  private static final List<DateTimeFormatter> JAVA_TIME_FORMATTERS =
      Arrays.asList(
          // ISO 标准格式
          DateTimeFormatter.ISO_INSTANT,
          DateTimeFormatter.ISO_ZONED_DATE_TIME,
          DateTimeFormatter.ISO_OFFSET_DATE_TIME,
          DateTimeFormatter.ISO_LOCAL_DATE_TIME,
          DateTimeFormatter.ISO_LOCAL_DATE,
          DateTimeFormatter.ISO_OFFSET_DATE,
          DateTimeFormatter.ISO_DATE,
          DateTimeFormatter.ISO_DATE_TIME,

          // RFC 标准格式
          DateTimeFormatter.RFC_1123_DATE_TIME,

          // 常见数据库/日志格式
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd"),
          DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"),
          DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
          DateTimeFormatter.ofPattern("yyyy/MM/dd"),
          DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"),
          DateTimeFormatter.ofPattern("yyyy.MM.dd"),

          // 美国格式
          DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
          DateTimeFormatter.ofPattern("MM/dd/yyyy"),
          DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"),
          DateTimeFormatter.ofPattern("MM-dd-yyyy"),

          // 欧洲格式
          DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
          DateTimeFormatter.ofPattern("dd/MM/yyyy"),
          DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
          DateTimeFormatter.ofPattern("dd-MM-yyyy"),

          // 中文格式
          DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒"),
          DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"),
          DateTimeFormatter.ofPattern("yyyy年MM月dd日"),

          // 时间戳相关格式
          DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"),
          DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
          DateTimeFormatter.ofPattern("yyyyMMdd"),
          DateTimeFormatter.ofPattern("yyMMddHHmmss"),

          // 带时区/偏移量
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"),
          DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
          DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),

          // 特殊格式
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"), // UTC
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
          DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"),
          DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssZ"));

  // Commons DateUtils 支持的格式
  private static final String[] COMMON_DATE_PATTERNS = {
    // ISO 格式
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
    "yyyy-MM-dd'T'HH:mm:ssZ",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd'T'HH:mm",

    // 标准日期时间
    "yyyy-MM-dd HH:mm:ss.SSS",
    "yyyy-MM-dd HH:mm:ss",
    "yyyy-MM-dd HH:mm",
    "yyyy-MM-dd",

    // 斜线格式
    "yyyy/MM/dd HH:mm:ss.SSS",
    "yyyy/MM/dd HH:mm:ss",
    "yyyy/MM/dd",

    // 点格式
    "yyyy.MM.dd HH:mm:ss",
    "yyyy.MM.dd",

    // 美国格式
    "MM/dd/yyyy HH:mm:ss",
    "MM/dd/yyyy",
    "MM-dd-yyyy HH:mm:ss",
    "MM-dd-yyyy",

    // 欧洲格式
    "dd/MM/yyyy HH:mm:ss",
    "dd/MM/yyyy",
    "dd-MM-yyyy HH:mm:ss",
    "dd-MM-yyyy",

    // 中文格式
    "yyyy年MM月dd日 HH:mm:ss",
    "yyyy年MM月dd日",

    // 紧凑格式
    "yyyyMMddHHmmssSSS",
    "yyyyMMddHHmmss",
    "yyyyMMdd",
    "yyMMdd",

    // RFC 格式
    "EEE, dd MMM yyyy HH:mm:ss zzz",
    "EEE MMM dd HH:mm:ss zzz yyyy",

    // 带时区
    "yyyy-MM-dd HH:mm:ss z",
    "yyyy-MM-dd HH:mm:ss Z",

    // UTC 格式
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
  };

  // 用于识别时间戳的正则模式
  private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^\\d{1,13}$");
  private static final Pattern MICROSECOND_PATTERN = Pattern.compile("^\\d{16}$");
  private static final Pattern NANOSECOND_PATTERN = Pattern.compile("^\\d{19}$");

  public long toMillis(Object value) {
    if (value == null) {
      throw new IllegalArgumentException("时间值不能为空");
    }

    // 如果是数字（时间戳）
    if (value instanceof Number n) {
      return handleNumericTimestamp(n.longValue());
    }

    // 如果是 Java 8+ 时间对象
    if (value instanceof java.time.temporal.TemporalAccessor ta) {
      return handleTemporalAccessor(ta);
    }

    // 如果是传统的 Date 对象
    if (value instanceof java.util.Date d) {
      return d.getTime();
    }

    // 如果是字符串
    if (value instanceof String str) {
      return parseStringToMillis(str);
    }

    // 如果是 Calendar
    if (value instanceof Calendar cal) {
      return cal.getTimeInMillis();
    }

    throw new IllegalArgumentException("不支持的时间格式: " + value.getClass().getName());
  }

  private long handleNumericTimestamp(long timestamp) {
    // 根据位数判断时间戳的精度
    String str = String.valueOf(timestamp);
    int length = str.length();

    switch (length) {
      case 10: // 秒级时间戳（常见于 Unix 时间戳）
        return timestamp * 1000L;
      case 13: // 毫秒级时间戳（Java 标准）
        return timestamp;
      case 16: // 微秒级时间戳
        return timestamp / 1000L;
      case 19: // 纳秒级时间戳
        return timestamp / 1_000_000L;
      default:
        // 其他位数，尝试智能判断
        if (timestamp > 1_000_000_000_000L) {
          // 大于 2001 年的毫秒数，按毫秒处理
          return timestamp;
        } else if (timestamp > 1_000_000L) {
          // 按秒处理
          return timestamp * 1000L;
        } else {
          // 太小的时间戳，可能是错误的
          throw new IllegalArgumentException("疑似无效的时间戳: " + timestamp);
        }
    }
  }

  private long handleTemporalAccessor(TemporalAccessor ta) {
    try {
      // 首先尝试直接获取 Instant
      return Instant.from(ta).toEpochMilli();
    } catch (DateTimeException e1) {
      try {
        // 尝试获取 ZonedDateTime
        return ZonedDateTime.from(ta).toInstant().toEpochMilli();
      } catch (DateTimeException e2) {
        try {
          // 尝试获取 OffsetDateTime
          return OffsetDateTime.from(ta).toInstant().toEpochMilli();
        } catch (DateTimeException e3) {
          try {
            // 尝试获取 LocalDateTime
            LocalDateTime ldt = LocalDateTime.from(ta);
            return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
          } catch (DateTimeException e4) {
            try {
              // 尝试获取 LocalDate
              LocalDate ld = LocalDate.from(ta);
              return ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeException e5) {
              try {
                // 尝试获取 YearMonth
                YearMonth ym = YearMonth.from(ta);
                return ym.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
              } catch (DateTimeException e6) {
                throw new IllegalArgumentException("无法从 TemporalAccessor 获取时间: " + ta, e6);
              }
            }
          }
        }
      }
    }
  }

  private long parseStringToMillis(String str) {
    // 清理字符串
    str = StringUtils.trimToEmpty(str);
    if (StringUtils.isEmpty(str)) {
      throw new IllegalArgumentException("时间字符串不能为空");
    }

    // 1. 首先检查是否是纯数字时间戳
    if (StringUtils.isNumeric(str)) {
      try {
        long timestamp = Long.parseLong(str);
        return handleNumericTimestamp(timestamp);
      } catch (NumberFormatException e) {
        // 继续尝试日期格式
      }
    }

    // 2. 尝试 Java Time API 的格式
    for (DateTimeFormatter formatter : JAVA_TIME_FORMATTERS) {
      try {
        TemporalAccessor temporal = formatter.parse(str);
        return handleTemporalAccessor(temporal);
      } catch (DateTimeParseException e) {
        // 格式不匹配，继续尝试下一个
      }
    }

    // 3. 使用 Commons DateUtils 尝试解析
    try {
      Date date = DateUtils.parseDate(str, COMMON_DATE_PATTERNS);
      return date.getTime();
    } catch (ParseException e) {
      // 继续尝试其他方法
    }

    // 4. 尝试一些特殊格式处理
    try {
      // 处理没有分隔符的日期时间
      if (str.matches("\\d{14}")) { // yyyyMMddHHmmss
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.parse(str).getTime();
      } else if (str.matches("\\d{8}")) { // yyyyMMdd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.parse(str).getTime();
      } else if (str.matches("\\d{6}")) { // yyMMdd
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        return sdf.parse(str).getTime();
      }
    } catch (ParseException e) {
      // 继续尝试
    }

    // 5. 尝试移除常见后缀
    String cleanedStr = cleanDateTimeString(str);
    if (!cleanedStr.equals(str)) {
      try {
        return parseStringToMillis(cleanedStr);
      } catch (IllegalArgumentException e) {
        // 继续尝试原始字符串的其他方法
      }
    }

    // 6. 尝试 SQL 格式（最后的手段）
    try {
      // 处理可能的 SQL 格式
      if (str.contains(".") && str.matches(".*\\d{1,9}$")) {
        // 可能是带纳秒的 SQL 格式
        String withoutNanos = str.replaceAll("\\.\\d{1,9}$", "");
        return java.sql.Timestamp.valueOf(withoutNanos).getTime();
      } else {
        return java.sql.Timestamp.valueOf(str).getTime();
      }
    } catch (IllegalArgumentException e) {
      // 7. 使用宽松的 SimpleDateFormat 作为最后尝试
      try {
        // 尝试自动清理并解析
        String normalized = normalizeDateTimeString(str);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setLenient(true);
        return sdf.parse(normalized).getTime();
      } catch (ParseException e2) {
        throw new IllegalArgumentException(
            String.format("无法解析的时间字符串: '%s'。支持的格式请参考方法文档。", str), e2);
      }
    }
  }

  private String cleanDateTimeString(String str) {
    // 移除常见的时间后缀
    String[] suffixes = {
      ".0", ".000", ".000000", ".000000000",
      " +08:00", " +0800", " UTC", " GMT",
      "上午", "下午", "AM", "PM"
    };

    String result = str;
    for (String suffix : suffixes) {
      if (result.endsWith(suffix)) {
        result = result.substring(0, result.length() - suffix.length());
      }
    }

    // 替换全角字符为半角
    result = result.replace('　', ' ').replace('，', ',').replace('；', ';').replace('：', ':');

    // 移除多余空格
    result = StringUtils.normalizeSpace(result);

    return result;
  }

  private String normalizeDateTimeString(String str) {
    String result = str.trim();

    // 处理完整的中文格式：2023年10月01日 10时30分45秒
    if (result.matches(".*[年月日时分秒].*")) {
      // 提取日期部分
      result = result.replace("年", "-").replace("月", "-").replace("日", " ");

      // 提取时间部分
      result = result.replace("时", ":").replace("分", ":").replace("秒", "");

      // 清理多余的空格和冒号
      result = result.replaceAll("\\s+", " ").replaceAll(":+", ":").replaceAll(":$", "").trim();
    }

    // 统一其他分隔符
    result = result.replace('/', '-').replace('.', '-').replace('：', ':');

    // 标准化空格
    result = StringUtils.normalizeSpace(result);

    // 补全缺失的时间部分
    if (result.matches("\\d{4}-\\d{1,2}-\\d{1,2}$")) {
      result += " 00:00:00";
    } else if (result.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}$")) {
      result += ":00:00";
    } else if (result.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}$")) {
      result += ":00";
    }

    // 补全前导零
    result = addLeadingZeros(result);

    return result;
  }

  private String addLeadingZeros(String dateStr) {
    // 将单个数字的月、日、时、分、秒补零
    String[] parts = dateStr.split("[- :]");
    if (parts.length >= 3) {
      // 补全月和日
      if (parts[1].length() == 1) parts[1] = "0" + parts[1];
      if (parts[2].length() == 1) parts[2] = "0" + parts[2];

      // 补全时间部分（如果有）
      if (parts.length > 3) {
        for (int i = 3; i < parts.length; i++) {
          if (parts[i].length() == 1) {
            parts[i] = "0" + parts[i];
          }
        }
      }

      // 重新组合
      StringBuilder sb = new StringBuilder();
      sb.append(parts[0]).append("-").append(parts[1]).append("-").append(parts[2]);
      if (parts.length > 3) {
        sb.append(" ").append(parts[3]);
        for (int i = 4; i < parts.length; i++) {
          sb.append(":").append(parts[i]);
        }
      }
      return sb.toString();
    }
    return dateStr;
  }

  // 工具方法：快速判断是否是有效的时间戳
  public static boolean isValidTimestamp(long timestamp) {
    // 合理的 Unix 时间戳范围：2001-01-01 到 2038-01-19（扩展后）
    long min = 978307200000L; // 2001-01-01
    long max = 2145916800000L; // 2038-01-19
    return timestamp >= min && timestamp <= max;
  }

  // 批量解析方法（优化性能）
  private final Map<String, DateTimeFormatter> formatterCache = new HashMap<>();

  private long parseWithCachedFormatter(String str, String pattern) {
    DateTimeFormatter formatter =
        formatterCache.computeIfAbsent(pattern, p -> DateTimeFormatter.ofPattern(p));

    try {
      TemporalAccessor temporal = formatter.parse(str);
      return handleTemporalAccessor(temporal);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(String.format("字符串 '%s' 不匹配格式 '%s'", str, pattern), e);
    }
  }
}
