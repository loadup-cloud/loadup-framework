package io.github.loadup.testify.starter.cases;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

/**
 * One data-driven test case loaded from a case file.
 *
 * @param name display name used by the parameterized test
 * @param variables per-case variables, resolved before the case runs
 * @param input raw input node, bound to the service method by the test code
 * @param expect expected output node (plain values or operator configs)
 */
public record TestifyCase(String name, Map<String, Object> variables, JsonNode input, JsonNode expect) {

    public TestifyCase {
        variables = variables == null ? Map.of() : variables;
    }

    @Override
    public String toString() {
        return name;
    }
}
