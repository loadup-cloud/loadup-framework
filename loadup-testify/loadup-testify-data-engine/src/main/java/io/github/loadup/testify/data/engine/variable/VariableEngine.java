/*
 * #%L
 * Testify Data Engine
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
package io.github.loadup.testify.data.engine.variable;

import io.github.loadup.commons.json.JsonUtil;
import io.github.loadup.testify.data.engine.function.TestifyFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.datafaker.Faker;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.MapAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import tools.jackson.databind.JsonNode;

/**
 * Variable resolution engine supporting: - Datafaker expressions: ${faker.name.firstName} - Time
 * functions with offsets: ${time.now('+1d')}, ${time.now('-2h')} - Custom functions: ${fn.uuid()},
 * ${fn.random(1, 100)} - Variable cross-references with dependency resolution
 */
public class VariableEngine {

    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final Map<String, TestifyFunction> functionRegistry = new HashMap<>();

    public VariableEngine(List<TestifyFunction> customFunctions) {

        if (customFunctions != null) {
            customFunctions.forEach(f -> functionRegistry.put(f.getPrefix(), f));
        }
    }

    public Map<String, Object> resolveVariables(Map<String, Object> rawVariables) {
        Map<String, Object> context = new LinkedHashMap<>();
        if (rawVariables == null) {
            return context;
        }

        int limit = 10;
        while (limit-- > 0) {
            boolean changed = false;
            for (Map.Entry<String, Object> entry : rawVariables.entrySet()) {
                String key = entry.getKey();
                if (context.containsKey(key)) {
                    continue;
                }

                Object raw = entry.getValue();
                Object evaluated = raw instanceof String s ? evaluate(s, context) : raw;

                if (!(evaluated instanceof String s) || !s.contains("${")) {
                    context.put(key, evaluated);
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }
        return context;
    }

    public Object evaluate(String text, Map<String, Object> context) {
        if (text == null || !text.contains("${")) {
            return text;
        }

        String trimmed = text.trim();

        if (trimmed.startsWith("${") && trimmed.endsWith("}") && countOccurrences(trimmed, "${") == 1) {
            return resolveSinglePlaceholder(trimmed, context);
        }

        return resolveMixedText(text, context);
    }

    private Object resolveSinglePlaceholder(String placeholder, Map<String, Object> context) {
        String expression = placeholder.substring(2, placeholder.length() - 1);
        String spelExpr = expression.startsWith("#") ? expression : "#" + expression;
        try {
            StandardEvaluationContext spelContext = buildSpelContext(context);
            Object value = spelParser.parseExpression(spelExpr).getValue(spelContext);
            return (value != null) ? value : placeholder;
        } catch (Exception e) {
            return placeholder;
        }
    }

    private String resolveMixedText(String text, Map<String, Object> context) {

        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        int lastCursor = 0;

        while (matcher.find()) {

            sb.append(text, lastCursor, matcher.start());

            String placeholder = matcher.group(0);
            Object resolved = resolveSinglePlaceholder(placeholder, context);

            sb.append(resolved);

            lastCursor = matcher.end();
        }
        sb.append(text.substring(lastCursor));
        return sb.toString();
    }

    private StandardEvaluationContext buildSpelContext(Map<String, Object> context) {
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.addPropertyAccessor(new MapAccessor());

        functionRegistry.forEach((prefix, functionInstance) -> {
            ctx.setVariable(prefix, functionInstance);
        });
        ctx.setVariable("faker", new Faker());

        if (context != null) {
            context.forEach(ctx::setVariable);
        }
        return ctx;
    }

    private int countOccurrences(String str, String sub) {
        return (str.length() - str.replace(sub, "").length()) / sub.length();
    }

    public Object resolveValue(Object value, Map<String, Object> context) {
        if (value == null) {
            return null;
        }

        if (value instanceof JsonNode node) {
            if (node.isString()) {
                return evaluate(node.asString(), context);
            }
            if (node.isNumber()) {
                return node.numberValue();
            }
            if (node.isBoolean()) {
                return node.booleanValue();
            }
            if (node.isNull()) {
                return null;
            }
            if (node.isObject()) {
                Map<String, Object> resolvedMap = new LinkedHashMap<>();
                node.properties().forEach(entry -> {
                    Object resolved = resolveValue(entry.getValue(), context);
                    if (resolved != null) {
                        resolvedMap.put(entry.getKey(), resolved);
                    }
                });
                return resolvedMap;
            }
            if (node.isArray()) {
                List<Object> resolvedList = new ArrayList<>();
                node.forEach(item -> {
                    Object resolved = resolveValue(item, context);

                    resolvedList.add(resolved);
                });
                return resolvedList;
            }
            return JsonUtil.convertValue(node, Object.class);
        }

        if (value instanceof String str) {
            return evaluate(str, context);
        }

        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> result = new LinkedHashMap<>(map.size());
            map.forEach((k, v) -> {
                Object resolved = resolveValue(v, context);
                if (resolved != null) {
                    result.put(k, resolved);
                }
            });
            return result;
        }

        if (value instanceof Collection<?> col) {
            return col.stream()
                    .map(i -> resolveValue(i, context))
                    .filter(Objects::nonNull)
                    .toList();
        }

        return value;
    }
}
