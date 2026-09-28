package io.github.loadup.testify.asserts.operator.impl;

import io.github.loadup.testify.asserts.model.MatchResult;
import java.math.BigDecimal;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Simple equality and inequality matcher. Handles null values properly.
 */
public class SimpleMatcher {
    public static MatchResult eq(Object actual, Object expected) {
        if (actual == null && expected == null) {
            return MatchResult.pass();
        }
        if (actual == null || expected == null) {
            return MatchResult.fail(actual, expected, "One of the values is null");
        }

        // 核心：处理数字和字符串的等值比对（如 Long 123 vs Integer 123）
        if (StringUtils.isNumeric(actual.toString()) && StringUtils.isNumeric(expected.toString())) {
            return new BigDecimal(actual.toString()).compareTo(new BigDecimal(expected.toString())) == 0
                    ? MatchResult.pass()
                    : MatchResult.fail(actual, expected, "Numeric values not equal");
        }

        return Objects.equals(actual, expected)
                ? MatchResult.pass()
                : MatchResult.fail(actual, expected, "Values are not equal");
    }
}
