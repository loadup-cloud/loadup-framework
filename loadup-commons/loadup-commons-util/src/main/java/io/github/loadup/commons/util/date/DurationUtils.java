package io.github.loadup.commons.util.date;

import java.time.Duration;

public class DurationUtils {
    private DurationUtils() {}

    /**
     * 解析字符串为Duration
     *
     * @param duration 字符串，格式为 PT1H2M3S
     * @return Duration
     */
    public static Duration parse(String duration) {
        String upper = duration.toUpperCase();
        if (!upper.startsWith("PT")) {
            upper = "PT" + upper;
        }
        return Duration.parse(upper);
    }

    public static long parseSeconds(String duration) {
        String upper = duration.toUpperCase();
        if (!upper.startsWith("PT")) {
            upper = "PT" + upper;
        }
        return Duration.parse(upper).getSeconds();
    }
}
