package io.github.loadup.testify.asserts.engine;

/*-
 * #%L
 * Testify Assert Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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

public class ResponseAssertEngine implements TestifyAssertEngine {
    private final VariableEngine variableEngine;

    public ResponseAssertEngine(VariableEngine variableEngine) {
        this.variableEngine = variableEngine;
    }

    @Override
    public String supportKey() {
        return "response";
    }

    @Override
    public void compare(JsonNode expectRes, Object actualRes, Map<String, Object> context, List<String> reportList) {
        JsonNode actualNode = JsonUtil.valueToTree(actualRes);
        List<FieldDiff> diffs = new ArrayList<>();

        // 将期待配置转为 Map，识别 JsonPath
        Map<String, JsonNode> expectMap =
                JsonUtil.convertValue(expectRes, new TypeReference<Map<String, JsonNode>>() {});

        expectMap.forEach((key, expNode) -> {
            // 解析变量（针对 op 中的 val 等）
            JsonNode resolvedExp = variableEngine.resolveJsonNode(expNode, context);

            if (key.startsWith("$")) {
                // --- 场景 A: JsonPath 匹配 ---
                try {
                    String actualJson = JsonUtil.toJson(actualRes);
                    // 使用 Jayway JsonPath 提取实际值
                    Object extractedAct = JsonPath.read(actualJson, key);
                    JsonNode actNode = JsonUtil.valueToTree(extractedAct);
                    recursiveCheck(key, resolvedExp, actNode, diffs);
                } catch (Exception e) {
                    diffs.add(new FieldDiff(key, "JsonPath exists", "Error/Missing", e.getMessage()));
                }
            } else {
                // --- 场景 B: 原有的递归全量匹配 ---
                recursiveCheck("res." + key, resolvedExp, actualNode.path(key), diffs);
            }
        });

        if (!diffs.isEmpty()) {
            throw new AssertionError(DiffReportBuilder.build("API Response Assertion", diffs));
        }
    }

    private void recursiveCheck(String path, JsonNode exp, JsonNode act, List<FieldDiff> diffs) {
        // 情况 A: 期望节点是操作符配置 (如 {op: approx, val: ...})
        if (isOperatorNode(exp)) {
            Map<String, Object> expVal = JsonUtil.convertValue(exp, new TypeReference<>() {});
            Object actVal = getValueFromJsonNode(act);

            // 复用数据库的 OperatorProcessor
            MatchResult result = OperatorProcessor.process(actVal, expVal);
            if (!result.isPassed()) {
                diffs.add(new FieldDiff(path, expVal.get("val"), actVal, result.message()));
            }
            return;
        }

        // 情况 B: 期望节点是普通的 JSON 对象(递归)
        if (exp.isObject()) {
            exp.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                recursiveCheck(path + "." + key, entry.getValue(), act.path(key), diffs);
            });
        }
        // 情况 C: 期望节点是数组(递归)
        else if (exp.isArray()) {
            if (!act.isArray() || exp.size() != act.size()) {
                diffs.add(new FieldDiff(path, "Size: " + exp.size(), "Size: " + act.size(), "Array size mismatch"));
            } else {
                for (int i = 0; i < exp.size(); i++) {
                    recursiveCheck(path + "[" + i + "]", exp.get(i), act.get(i), diffs);
                }
            }
        }
        // 情况 D: 叶子节点，直接进行等值对比
        else {
            Object expVal = getValueFromJsonNode(exp);
            Object actVal = getValueFromJsonNode(act);
            // 统一走 OperatorProcessor，这样即使是普通值，也能享受 SimpleMatcher 提供的类型兼容性比对
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
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        if (node.isNumber()) return node.numberValue();
        if (node.isBoolean()) return node.booleanValue();
        return node.asText();
    }
}
