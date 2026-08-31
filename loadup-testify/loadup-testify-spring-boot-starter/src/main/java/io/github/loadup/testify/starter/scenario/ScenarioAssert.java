package io.github.loadup.testify.starter.scenario;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.loadup.testify.asserts.engine.DbAssertEngine;
import io.github.loadup.testify.asserts.engine.ExceptionAssertEngine;
import io.github.loadup.testify.asserts.engine.ResponseAssertEngine;
import io.github.loadup.testify.asserts.model.MatchResult;
import io.github.loadup.testify.asserts.operator.OperatorProcessor;
import io.github.loadup.testify.core.util.JsonUtil;
import java.util.Map;

/**
 * Declarative assertion helper for integration tests. Compares an actual result against an expected
 * structure (plain values or operator configs) instead of hand-written assertion chains.
 *
 * <p>Expected values support {@code {op: ..., val: ...}} operator configs: {@code eq}, {@code
 * approx} (time, optional {@code threshold} in ms), {@code contains}, {@code regex}, {@code json}
 * (lenient by default, {@code mode: full} for strict), {@code size}, {@code gt}/{@code ge}/{@code
 * lt}/{@code le}. Expected map keys starting with {@code $} are evaluated as JsonPath against the
 * actual result. Placeholders like {@code ${var}} are resolved against the variables passed to each
 * method.
 */
public class ScenarioAssert {

    private final ResponseAssertEngine responseAssertEngine;
    private final DbAssertEngine dbAssertEngine;
    private final ExceptionAssertEngine exceptionAssertEngine;

    public ScenarioAssert(
            ResponseAssertEngine responseAssertEngine,
            DbAssertEngine dbAssertEngine,
            ExceptionAssertEngine exceptionAssertEngine) {
        this.responseAssertEngine = responseAssertEngine;
        this.dbAssertEngine = dbAssertEngine;
        this.exceptionAssertEngine = exceptionAssertEngine;
    }

    public ScenarioAssert verify(Object actual, Object expected) {
        return verify(actual, expected, Map.of());
    }

    /**
     * Verify an actual result against an expected structure.
     *
     * @param actual the object returned by the code under test
     * @param expected a Map, JsonNode or JSON string describing expected values
     * @param variables resolved variables used to expand {@code ${...}} placeholders
     */
    public ScenarioAssert verify(Object actual, Object expected, Map<String, Object> variables) {
        JsonNode expectNode = toNode(expected);
        if (!expectNode.isObject() || expectNode.isEmpty()) {
            MatchResult result = OperatorProcessor.process(actual, leafValue(expectNode));
            if (!result.isPassed()) {
                throw new AssertionError("Assertion failed: " + result.message());
            }
            return this;
        }
        responseAssertEngine.compare(expectNode, actual, variables == null ? Map.of() : variables);
        return this;
    }

    public ScenarioAssert verifyJson(String actualJson, Object expected) {
        return verifyJson(actualJson, expected, Map.of());
    }

    /**
     * Verify a raw JSON string response against the expected structure.
     */
    public ScenarioAssert verifyJson(String actualJson, Object expected, Map<String, Object> variables) {
        return verify(JsonUtil.readTree(actualJson), expected, variables);
    }

    public ScenarioAssert verifyDb(Object expected) {
        return verifyDb(expected, Map.of());
    }

    /**
     * Verify database state, e.g. {@code {table: users, mode: strict, rows: [...]}}.
     */
    public ScenarioAssert verifyDb(Object expected, Map<String, Object> variables) {
        dbAssertEngine.compare(toNode(expected), null, variables == null ? Map.of() : variables);
        return this;
    }

    public ScenarioAssert verifyException(Throwable actual, Object expected) {
        return verifyException(actual, expected, Map.of());
    }

    /**
     * Verify a thrown exception, e.g. {@code {type: ..., message: ...}}.
     */
    public ScenarioAssert verifyException(Throwable actual, Object expected, Map<String, Object> variables) {
        exceptionAssertEngine.compare(toNode(expected), actual, variables == null ? Map.of() : variables);
        return this;
    }

    private JsonNode toNode(Object value) {
        if (value instanceof JsonNode node) {
            return node;
        }
        if (value instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                return JsonUtil.readTree(text);
            }
        }
        return JsonUtil.valueToTree(value);
    }

    private Object leafValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return node.asText();
    }
}
