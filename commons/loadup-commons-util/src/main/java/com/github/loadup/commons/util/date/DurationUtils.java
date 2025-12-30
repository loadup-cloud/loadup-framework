package com.github.loadup.commons.util.date;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

public class DurationUtils extends org.apache.commons.lang3.time.DurationUtils {
    private DurationUtils() {}

    /**
     * 解析字符串为Duration
     *
     * @param duration 字符串，格式为 PT1H2M3S
     * @return Duration
     */
    public static Duration parse(String duration) {
        if (!StringUtils.startsWithIgnoreCase(duration, "PT")) {
            duration = "PT" + duration;
        }
        return Duration.parse(duration.toUpperCase());
    }

    public static long parseSeconds(String duration) {
        if (!StringUtils.startsWithIgnoreCase(duration, "PT")) {
            duration = "PT" + duration;
        }
        Duration parse = Duration.parse(duration.toUpperCase());
        return parse.getSeconds();
    }
}
