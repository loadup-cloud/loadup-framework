package com.github.loadup.commons.util.date;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

public class DurationUtils extends org.apache.commons.lang3.time.DurationUtils {
    private DurationUtils() {
    }

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
