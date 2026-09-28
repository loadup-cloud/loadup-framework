package io.github.loadup.testify.asserts.model;

public record MatchResult(boolean isPassed, Object actual, Object expected, String message) {
    public static MatchResult pass() {
        return new MatchResult(true, null, null, null);
    }

    public static MatchResult fail(Object act, Object exp, String msg) {
        return new MatchResult(false, act, exp, msg);
    }
}
