/*
 * #%L
 * LoadUp Common JSON
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
package io.github.loadup.commons.json;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Immutable, strict formatters shared by JSON temporal readers. */
final class TemporalFormats {
    private static final List<String> DATE_PATTERNS = List.of("uuuu-MM-dd", "uuuu/MM/dd", "uuuu.MM.dd", "uuuuMMdd");
    private static final List<DateTimeFormatter> DATES = DATE_PATTERNS.stream()
            .map(pattern -> DateTimeFormatter.ofPattern(pattern, Locale.ROOT).withResolverStyle(ResolverStyle.STRICT))
            .toList();
    private static final List<DateTimeFormatter> DATE_TIMES = dateTimeFormatters();

    private TemporalFormats() {}

    static LocalDate localDate(String text) {
        for (DateTimeFormatter formatter : DATES) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next complete format.
            }
        }
        throw unsupported(text);
    }

    static LocalDateTime localDateTime(String text) {
        for (DateTimeFormatter formatter : DATE_TIMES) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next complete format.
            }
        }
        throw unsupported(text);
    }

    static Instant instant(String text, ZoneId zone) {
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ISO_ZONED_DATE_TIME,
                DateTimeFormatter.RFC_1123_DATE_TIME.withResolverStyle(ResolverStyle.STRICT))) {
            try {
                return ZonedDateTime.parse(text, formatter).toInstant();
            } catch (DateTimeParseException ignored) {
                // Zoned input preserves its own instant.
            }
        }
        try {
            return localDateTime(text).atZone(zone).toInstant();
        } catch (DateTimeParseException ignored) {
            return localDate(text).atStartOfDay(zone).toInstant();
        }
    }

    private static List<DateTimeFormatter> dateTimeFormatters() {
        List<DateTimeFormatter> formatters = new ArrayList<>();
        for (String pattern : DATE_PATTERNS.subList(0, 3)) {
            for (char separator : new char[] {' ', 'T'}) {
                formatters.add(new DateTimeFormatterBuilder()
                        .appendPattern(pattern)
                        .appendLiteral(separator)
                        .appendPattern("HH:mm")
                        .optionalStart()
                        .appendLiteral(':')
                        .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                        .optionalStart()
                        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                        .optionalEnd()
                        .optionalEnd()
                        .toFormatter(Locale.ROOT)
                        .withResolverStyle(ResolverStyle.STRICT));
            }
        }
        formatters.add(new DateTimeFormatterBuilder()
                .appendPattern("uuuuMMddHHmmss")
                .optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                .optionalEnd()
                .toFormatter(Locale.ROOT)
                .withResolverStyle(ResolverStyle.STRICT));
        return List.copyOf(formatters);
    }

    private static DateTimeParseException unsupported(String text) {
        return new DateTimeParseException("Unsupported or invalid temporal format", text, 0);
    }
}
