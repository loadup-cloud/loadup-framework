package com.github.loadup.components.testify.comparator;

import java.util.Map;

public class DeepComparator {

    public static boolean deepEquals(Object expected, Object actual) {
        if (expected instanceof Map && actual instanceof Map) {
            Map<?, ?> expectedMap = (Map<?, ?>) expected;
            Map<?, ?> actualMap = (Map<?, ?>) actual;

            for (Object key : expectedMap.keySet()) {
                if (!actualMap.containsKey(key)) return false;
                if (!deepEquals(expectedMap.get(key), actualMap.get(key))) return false;
            }
            return true;
        } else {
            return expected.equals(actual);
        }
    }
}
