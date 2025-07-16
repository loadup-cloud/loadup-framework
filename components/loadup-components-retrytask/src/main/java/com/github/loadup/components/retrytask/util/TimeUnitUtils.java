package com.github.loadup.components.retrytask.util;

public class TimeUnitUtils {

    public static long toMillis(long value, String unit) {
        switch (unit.toUpperCase()) {
            case "MILLISECONDS":
                return value;
            case "SECONDS":
                return value * 1000;
            case "MINUTES":
                return value * 60 * 1000;
            case "HOURS":
                return value * 60 * 60 * 1000;
            default:
                throw new IllegalArgumentException("Unsupported time unit: " + unit);
        }
    }

    public static long[] toMillisArray(String[] values, String unit) {
        long[] result = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = toMillis(Long.parseLong(values[i]), unit);
        }
        return result;
    }
}
