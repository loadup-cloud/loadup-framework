/*
 * #%L
 * Testify Assert Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.testify.asserts.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

public class TimeParser {

    private static final List<DateTimeFormatter> JAVA_TIME_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ISO_OFFSET_DATE,
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.RFC_1123_DATE_TIME,
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
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒"),
            DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy年MM月dd日"),
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"),
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyMMddHHmmss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"),
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"), // UTC
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"),
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssZ"));

    private static final String[] COMMON_DATE_PATTERNS = {
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm",
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd",
        "yyyy/MM/dd HH:mm:ss.SSS",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd",
        "yyyy.MM.dd HH:mm:ss",
        "yyyy.MM.dd",
        "MM/dd/yyyy HH:mm:ss",
        "MM/dd/yyyy",
        "MM-dd-yyyy HH:mm:ss",
        "MM-dd-yyyy",
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy",
        "dd-MM-yyyy HH:mm:ss",
        "dd-MM-yyyy",
        "yyyy年MM月dd日 HH:mm:ss",
        "yyyy年MM月dd日",
        "yyyyMMddHHmmssSSS",
        "yyyyMMddHHmmss",
        "yyyyMMdd",
        "yyMMdd",
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "EEE MMM dd HH:mm:ss zzz yyyy",
        "yyyy-MM-dd HH:mm:ss z",
        "yyyy-MM-dd HH:mm:ss Z",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    };

    public static boolean isValidTimestamp(long timestamp) {

        long min = 978307200000L; // 2001-01-01
        long max = 2145916800000L; // 2038-01-19
        return timestamp >= min && timestamp <= max;
    }

    public long toMillis(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("时间值不能为空");
        }

        if (value instanceof Number n) {
            return handleNumericTimestamp(n.longValue());
        }

        if (value instanceof TemporalAccessor ta) {
            return handleTemporalAccessor(ta);
        }

        if (value instanceof Date d) {
            return d.getTime();
        }

        if (value instanceof String str) {
            return parseStringToMillis(str);
        }

        if (value instanceof Calendar cal) {
            return cal.getTimeInMillis();
        }

        throw new IllegalArgumentException("不支持的时间格式: " + value.getClass().getName());
    }

    private long handleNumericTimestamp(long timestamp) {

        String str = String.valueOf(timestamp);
        int length = str.length();

        switch (length) {
            case 10:
                return timestamp * 1000L;
            case 13:
                return timestamp;
            case 16:
                return timestamp / 1000L;
            case 19:
                return timestamp / 1_000_000L;
            default:
                if (timestamp > 1_000_000_000_000L) {

                    return timestamp;
                } else if (timestamp > 1_000_000L) {

                    return timestamp * 1000L;
                } else {

                    throw new IllegalArgumentException("疑似无效的时间戳: " + timestamp);
                }
        }
    }

    private long handleTemporalAccessor(TemporalAccessor ta) {
        try {

            return Instant.from(ta).toEpochMilli();
        } catch (DateTimeException e1) {
            try {

                return ZonedDateTime.from(ta).toInstant().toEpochMilli();
            } catch (DateTimeException e2) {
                try {

                    return OffsetDateTime.from(ta).toInstant().toEpochMilli();
                } catch (DateTimeException e3) {
                    try {

                        LocalDateTime ldt = LocalDateTime.from(ta);
                        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    } catch (DateTimeException e4) {
                        try {

                            LocalDate ld = LocalDate.from(ta);
                            return ld.atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli();
                        } catch (DateTimeException e5) {
                            try {

                                YearMonth ym = YearMonth.from(ta);
                                return ym.atDay(1)
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .toInstant()
                                        .toEpochMilli();
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

        str = StringUtils.trimToEmpty(str);
        if (StringUtils.isEmpty(str)) {
            throw new IllegalArgumentException("时间字符串不能为空");
        }

        if (StringUtils.isNumeric(str)) {
            try {
                long timestamp = Long.parseLong(str);
                return handleNumericTimestamp(timestamp);
            } catch (NumberFormatException ignored) {

            }
        }

        for (DateTimeFormatter formatter : JAVA_TIME_FORMATTERS) {
            try {
                TemporalAccessor temporal = formatter.parse(str);
                return handleTemporalAccessor(temporal);
            } catch (DateTimeParseException ignored) {

            }
        }

        try {
            Date date = DateUtils.parseDate(str, COMMON_DATE_PATTERNS);
            return date.getTime();
        } catch (ParseException ignored) {

        }

        try {

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
        } catch (ParseException ignored) {

        }

        String cleanedStr = cleanDateTimeString(str);
        if (!cleanedStr.equals(str)) {
            try {
                return parseStringToMillis(cleanedStr);
            } catch (IllegalArgumentException ignored) {

            }
        }

        try {

            if (str.contains(".") && str.matches(".*\\d{1,9}$")) {

                String withoutNanos = str.replaceAll("\\.\\d{1,9}$", "");
                return java.sql.Timestamp.valueOf(withoutNanos).getTime();
            } else {
                return java.sql.Timestamp.valueOf(str).getTime();
            }
        } catch (IllegalArgumentException ignored) {

            try {

                String normalized = normalizeDateTimeString(str);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setLenient(true);
                return sdf.parse(normalized).getTime();
            } catch (ParseException e2) {
                throw new IllegalArgumentException(String.format("无法解析的时间字符串: '%s'。支持的格式请参考方法文档。", str), e2);
            }
        }
    }

    private String cleanDateTimeString(String str) {

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

        result = result.replace('　', ' ').replace('，', ',').replace('；', ';').replace('：', ':');

        result = StringUtils.normalizeSpace(result);

        return result;
    }

    private String normalizeDateTimeString(String str) {
        String result = str.trim();

        if (result.matches(".*[年月日时分秒].*")) {

            result = result.replace("年", "-").replace("月", "-").replace("日", " ");

            result = result.replace("时", ":").replace("分", ":").replace("秒", "");

            result = result.replaceAll("\\s+", " ")
                    .replaceAll(":+", ":")
                    .replaceAll(":$", "")
                    .trim();
        }

        result = result.replace('/', '-').replace('.', '-').replace('：', ':');

        result = StringUtils.normalizeSpace(result);

        if (result.matches("\\d{4}-\\d{1,2}-\\d{1,2}$")) {
            result += " 00:00:00";
        } else if (result.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}$")) {
            result += ":00:00"; // NOPMD AvoidUsingHardCodedIP - time padding, not a network address.
        } else if (result.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}$")) {
            result += ":00";
        }

        result = addLeadingZeros(result);

        return result;
    }

    private String addLeadingZeros(String dateStr) {

        String[] parts = dateStr.split("[- :]");
        if (parts.length >= 3) {

            if (parts[1].length() == 1) {
                parts[1] = "0" + parts[1];
            }
            if (parts[2].length() == 1) {
                parts[2] = "0" + parts[2];
            }

            if (parts.length > 3) {
                for (int i = 3; i < parts.length; i++) {
                    if (parts[i].length() == 1) {
                        parts[i] = "0" + parts[i];
                    }
                }
            }

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
}
