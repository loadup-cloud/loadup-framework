/*
 * #%L
 * Testify Assert Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.testify.asserts.engine;

import com.jayway.jsonpath.JsonPath;
import io.github.loadup.testify.asserts.diff.DiffReportBuilder;
import io.github.loadup.testify.asserts.model.FieldDiff;
import io.github.loadup.testify.asserts.model.MatchResult;
import io.github.loadup.testify.asserts.operator.OperatorProcessor;
import io.github.loadup.testify.core.util.JsonUtil;
import io.github.loadup.testify.data.engine.variable.VariableEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

public class ResponseAssertEngine implements TestifyAssertEngine {
    private final VariableEngine variableEngine;

    public ResponseAssertEngine(VariableEngine variableEngine) {
        this.variableEngine = variableEngine;
    }

    @Override
    public void compare(JsonNode expectRes, Object actualRes, Map<String, Object> context) {
        JsonNode actualNode = JsonUtil.valueToTree(actualRes);
        List<FieldDiff> diffs = new ArrayList<>();

        Map<String, JsonNode> expectMap =
                JsonUtil.convertValue(expectRes, new TypeReference<Map<String, JsonNode>>() {});

        expectMap.forEach((key, expNode) -> {
            Object resolved = variableEngine.resolveValue(expNode, context);
            JsonNode resolvedExp = JsonUtil.valueToTree(resolved);

            if (key.startsWith("$")) {

                try {
                    String actualJson = JsonUtil.toJson(actualRes);

                    Object extractedAct = JsonPath.read(actualJson, key);
                    JsonNode actNode = JsonUtil.valueToTree(extractedAct);
                    recursiveCheck(key, resolvedExp, actNode, diffs);
                } catch (Exception e) {
                    diffs.add(new FieldDiff(key, "JsonPath exists", "Error/Missing", e.getMessage()));
                }
            } else {

                recursiveCheck("res." + key, resolvedExp, actualNode.path(key), diffs);
            }
        });

        if (!diffs.isEmpty()) {
            throw new AssertionError(DiffReportBuilder.build("API Response Assertion", diffs));
        }
    }

    private void recursiveCheck(String path, JsonNode exp, JsonNode act, List<FieldDiff> diffs) {

        if (isOperatorNode(exp)) {
            Map<String, Object> expVal = JsonUtil.convertValue(exp, new TypeReference<>() {});
            Object actVal = getValueFromJsonNode(act);

            MatchResult result = OperatorProcessor.process(actVal, expVal);
            if (!result.isPassed()) {
                diffs.add(new FieldDiff(path, expVal.get("val"), actVal, result.message()));
            }
            return;
        }

        if (exp.isObject()) {
            exp.properties().forEach(entry -> {
                String key = entry.getKey();
                recursiveCheck(path + "." + key, entry.getValue(), act.path(key), diffs);
            });

        } else if (exp.isArray()) {
            if (!act.isArray() || exp.size() != act.size()) {
                diffs.add(new FieldDiff(path, "Size: " + exp.size(), "Size: " + act.size(), "Array size mismatch"));
            } else {
                for (int i = 0; i < exp.size(); i++) {
                    recursiveCheck(path + "[" + i + "]", exp.get(i), act.get(i), diffs);
                }
            }

        } else {
            Object expVal = getValueFromJsonNode(exp);
            Object actVal = getValueFromJsonNode(act);

            MatchResult result = OperatorProcessor.process(actVal, expVal);
            if (!result.isPassed()) {
                diffs.add(new FieldDiff(path, expVal, actVal, result.message()));
            }
        }
    }

    private boolean isOperatorNode(JsonNode node) {
        return node.isObject() && node.has("op") && node.has("val");
    }

    private Object getValueFromJsonNode(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isArray()) {
            return JsonUtil.convertValue(node, new TypeReference<List<Object>>() {});
        }
        return node.asString();
    }
}
