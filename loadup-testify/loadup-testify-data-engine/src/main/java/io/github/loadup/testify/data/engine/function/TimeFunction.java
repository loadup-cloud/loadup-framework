package io.github.loadup.testify.data.engine.function;

/*-
 * #%L
 * Testify Data Engine
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Time functions accessible via ${time.XXX()} Supports offset calculations like ${time.now('+1d')}
 */
public class TimeFunction implements TestifyFunction {
    @Override
    public String getPrefix() {
        return "time";
    }

    // Pattern to extract time offset like '+1d' or '-2h'
    private static final Pattern TIME_OFFSET_PATTERN = Pattern.compile("([+-])(\\d+)([hdms])");

    /** Get current timestamp as ISO-8601 string. */
    public String now() {
        return LocalDateTime.now().toString();
    }

    /**
     * Get current timestamp with offset. Supported formats: - '+1d' or '-1d' for days - '+2h' or
     * '-2h' for hours - '+30m' or '-30m' for minutes - '+60s' or '-60s' for seconds
     */
    public String now(String offset) {
        LocalDateTime base = LocalDateTime.now();
        LocalDateTime result = applyOffset(base, offset);
        return result.toString();
    }

    /** Get current date/time formatted with pattern. */
    public String format(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    /** Get current date/time with offset and format. */
    public String format(String offset, String pattern) {
        LocalDateTime base = LocalDateTime.now();
        LocalDateTime result = applyOffset(base, offset);
        return result.format(DateTimeFormatter.ofPattern(pattern));
    }

    /** Get current epoch milliseconds. */
    public long epochMilli() {
        return System.currentTimeMillis();
    }

    /** Get epoch milliseconds with offset. */
    public long epochMilli(String offset) {
        LocalDateTime base = LocalDateTime.now();
        LocalDateTime result = applyOffset(base, offset);
        return result.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // Apply time offset like '+1d', '-2h', etc.
    private LocalDateTime applyOffset(LocalDateTime base, String offset) {
        if (offset == null || offset.isBlank()) {
            return base;
        }

        Matcher matcher = TIME_OFFSET_PATTERN.matcher(offset);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time offset format: "
                    + offset
                    + ". Expected format: [+/-]<number>[d|h|m|s], e.g., '+1d' or '-2h'");
        }

        String sign = matcher.group(1);
        int amount = Integer.parseInt(matcher.group(2));
        String unit = matcher.group(3);

        if ("-".equals(sign)) {
            amount = -amount;
        }

        return switch (unit) {
            case "d" -> base.plusDays(amount);
            case "h" -> base.plusHours(amount);
            case "m" -> base.plusMinutes(amount);
            case "s" -> base.plusSeconds(amount);
            default -> throw new IllegalArgumentException("Unknown time unit: " + unit);
        };
    }

    /** 返回 LocalDateTime 对象而非字符串，用于数据库直接比较 */
    public LocalDateTime nativeNow(String offset) {
        return applyOffset(LocalDateTime.now(), offset);
    }

    /** 获取今天凌晨的时间 */
    public LocalDateTime startOfDay(String offset) {
        return applyOffset(LocalDateTime.now(), offset)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }
}
