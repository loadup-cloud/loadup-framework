package io.github.loadup.testify.test.asserts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.github.loadup.testify.asserts.engine.DbAssertEngine;
import io.github.loadup.testify.asserts.engine.ExceptionAssertEngine;
import io.github.loadup.testify.asserts.engine.ResponseAssertEngine;
import io.github.loadup.testify.data.engine.function.CommonFunction;
import io.github.loadup.testify.data.engine.function.TimeFunction;
import io.github.loadup.testify.data.engine.variable.VariableEngine;
import io.github.loadup.testify.starter.scenario.ScenarioAssert;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScenarioAssertTest {

    private final VariableEngine variableEngine = new VariableEngine(List.of(new TimeFunction(), new CommonFunction()));

    private final ScenarioAssert scenarioAssert = new ScenarioAssert(
            new ResponseAssertEngine(variableEngine),
            new DbAssertEngine(null, variableEngine),
            new ExceptionAssertEngine(variableEngine));

    @Test
    void plainEqualityIgnoresNumericType() {
        assertDoesNotThrow(() -> scenarioAssert.verify(Map.of("id", 123L), Map.of("id", 123)));
    }

    @Test
    void regexMatchesEmailFormat() {
        assertDoesNotThrow(() -> scenarioAssert.verify(
                Map.of("email", "ada.lovelace@example.com"),
                Map.of("email", Map.of("op", "regex", "val", "^[a-zA-Z0-9._%+-]+@example\\.com$"))));
    }

    @Test
    void containsMatchesSubstring() {
        assertDoesNotThrow(() -> scenarioAssert.verify(
                Map.of("name", "Grace Hopper"), Map.of("name", Map.of("op", "contains", "val", "Hopper"))));
    }

    @Test
    void approxMatchesTimeWithinThreshold() {
        LocalDateTime expected = LocalDateTime.now();
        assertDoesNotThrow(() -> scenarioAssert.verify(
                Map.of("createdAt", LocalDateTime.now()),
                Map.of("createdAt", Map.of("op", "approx", "val", expected.toString()))));
    }

    @Test
    void jsonOpSupportsLenientPartialMatch() {
        assertDoesNotThrow(() -> scenarioAssert.verify(
                Map.of("payload", "{\"a\":1,\"b\":2}"), Map.of("payload", Map.of("op", "json", "val", "{\"a\":1}"))));
    }

    @Test
    void jsonOpStrictRejectsExtraFields() {
        assertThatThrownBy(() -> scenarioAssert.verify(
                        Map.of("payload", "{\"a\":1,\"b\":2}"),
                        Map.of("payload", Map.of("op", "json", "mode", "full", "val", "{\"a\":1}"))))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void numberOperatorsCompareCorrectly() {
        assertDoesNotThrow(
                () -> scenarioAssert.verify(Map.of("age", 30), Map.of("age", Map.of("op", "gt", "val", 18))));
        assertThatThrownBy(() -> scenarioAssert.verify(Map.of("age", 10), Map.of("age", Map.of("op", "gt", "val", 18))))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void sizeOperatorValidatesListLength() {
        assertDoesNotThrow(() -> scenarioAssert.verify(
                Map.of("tags", List.of("a", "b")), Map.of("tags", Map.of("op", "size", "val", 2))));
    }

    @Test
    void jsonPathKeyExtractsNestedValue() {
        assertDoesNotThrow(() -> scenarioAssert.verify(
                Map.of("order", Map.of("orderName", "Alan Turing")), Map.of("$.order.orderName", "Alan Turing")));
    }

    @Test
    void variablesAreResolvedBeforeComparison() {
        assertDoesNotThrow(
                () -> scenarioAssert.verify(Map.of("id", "u-1"), Map.of("id", "${userId}"), Map.of("userId", "u-1")));
    }

    @Test
    void failureProducesDiffReport() {
        assertThatThrownBy(() -> scenarioAssert.verify(Map.of("id", "a"), Map.of("id", "b")))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("res.id");
    }

    @Test
    void verifyJsonParsesActualString() {
        assertDoesNotThrow(() -> scenarioAssert.verifyJson(
                "{\"id\":\"u-1\",\"name\":\"Ada\"}",
                Map.of("id", "u-1", "name", Map.of("op", "regex", "val", "^Ada$"))));
    }
}
