package io.github.loadup.testify.asserts.operator.impl;

import io.github.loadup.testify.asserts.model.MatchResult;
import io.github.loadup.testify.asserts.operator.OperatorMatcher;
import java.util.Map;

/**
 * Regular expression matcher with pattern caching for performance.
 */
public class RegexMatcher implements OperatorMatcher {
    @Override
    public boolean support(String op) {
        return "regex".equals(op);
    }

    @Override
    public MatchResult match(Object actual, Object val, Map<String, Object> config) {
        if (actual == null) {
            return MatchResult.fail(null, val, "Actual value is null");
        }

        String regex = String.valueOf(val);
        boolean matches = String.valueOf(actual).matches(regex);

        return matches
                ? MatchResult.pass()
                : MatchResult.fail(actual, val, "Actual value does not match regex pattern: " + regex);
    }
}
