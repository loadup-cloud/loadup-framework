/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.id;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.database.sequence.DatabaseResultCodeEnum;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * ASCII encoding with an 18-character prefix and 14-character fixed tail.
 */
public final class BusinessIdFormatter {
    public static final long MAX_SEQUENCE = 99_999_999L;
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

    public String format(BusinessIdParts parts) {
        if (parts == null
                || parts.date() == null
                || parts.command() == null
                || parts.sequence() < 1
                || parts.sequence() > MAX_SEQUENCE
                || parts.date().getYear() < 1
                || parts.date().getYear() > 9999) {
            throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT);
        }
        BusinessIdCommand c = parts.command();
        return DATE.format(parts.date())
                + c.dataVersion()
                + c.systemVersion()
                + c.eventCode()
                + c.extensionCode()
                + digits(c.siteId(), 3)
                + digits(c.shard(), 3)
                + digits(parts.sequence(), 8);
    }

    public BusinessIdParts parse(String value) {
        if (value == null
                || value.length() < 32
                || value.length() > 64
                || !value.substring(0, 18).matches("[0-9]{18}")
                || !value.substring(value.length() - 14).matches("[0-9]{14}")) {
            throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT);
        }
        try {
            int tail = value.length() - 14;
            BusinessIdCommand c = new BusinessIdCommand(
                    value.charAt(8) - '0',
                    value.charAt(9) - '0',
                    value.substring(10, 18),
                    value.substring(18, tail),
                    Integer.parseInt(value.substring(tail, tail + 3)),
                    Integer.parseInt(value.substring(tail + 3, tail + 6)));
            BusinessIdParts parts = new BusinessIdParts(
                    LocalDate.parse(value.substring(0, 8), DATE), c, Long.parseLong(value.substring(tail + 6)));
            format(parts);
            return parts;
        } catch (java.time.DateTimeException | NumberFormatException failure) {
            throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT, failure);
        }
    }

    private String digits(long value, int width) {
        String text = Long.toString(value);
        return "0".repeat(width - text.length()) + text;
    }
}
