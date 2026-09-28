package io.github.loadup.testify.asserts.operator.impl;

import io.github.loadup.testify.asserts.model.MatchResult;
import io.github.loadup.testify.asserts.operator.OperatorMatcher;
import java.util.Collection;
import java.util.Map;

/**
 * Simple equality and inequality matcher. Handles null values properly.
 */
public class ListMatcher implements OperatorMatcher {
    @Override
    public boolean support(String op) {
        return "size".equals(op);
    }

    @Override
    public MatchResult match(Object actual, Object val, Map<String, Object> config) {
        int actStr = 0;
        if (actual instanceof Collection list) {
            actStr = list.size();
        }

        int expStr = Integer.parseInt(String.valueOf(val));

        boolean matched = actStr == expStr;
        return matched
                ? MatchResult.pass()
                : MatchResult.fail(actual, val, "Actual List size does not matched expected size");
    }
}
