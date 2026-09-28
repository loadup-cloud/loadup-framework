package io.github.loadup.testify.asserts.operator.impl;

import io.github.loadup.testify.asserts.model.MatchResult;
import io.github.loadup.testify.asserts.operator.OperatorMatcher;
import java.util.Map;

/**
 * Simple equality and inequality matcher. Handles null values properly.
 */
public class StringMatcher implements OperatorMatcher {
    @Override
    public boolean support(String op) {
        return "contains".equals(op);
    }

    @Override
    public MatchResult match(Object actual, Object val, Map<String, Object> config) {
        String actStr = String.valueOf(actual);
        String expStr = String.valueOf(val);

        boolean matched = actStr.contains(expStr);
        return matched
                ? MatchResult.pass()
                : MatchResult.fail(actual, val, "Actual string does not contain expected substring");
    }
}
