package com.github.loadup.commons.util.date;


import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

public class DurationUtils  extends org.apache.commons.lang3.time.DurationUtils {
    private DurationUtils() {}

    /**
     * 解析字符串为Duration
     * @param duration 字符串，格式为 PT1H2M3S
     * @return Duration
     */
    public static Duration parse(String duration) {
        if (!StringUtils.startsWithIgnoreCase(duration, "PT")) {
            duration = "PT" + duration;
        }
        return Duration.parse(duration.toUpperCase());
    }
}
