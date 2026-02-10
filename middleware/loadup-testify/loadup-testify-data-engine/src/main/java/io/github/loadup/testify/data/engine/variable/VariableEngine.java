package io.github.loadup.testify.data.engine.variable;

/*-
 * #%L
 * Testify Data Engine
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

import com.fasterxml.jackson.databind.JsonNode;
import io.github.loadup.testify.core.util.JsonUtil;
import io.github.loadup.testify.data.engine.function.TestifyFunction;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Variable resolution engine supporting: - Datafaker expressions: ${faker.name.firstName} - Time
 * functions with offsets: ${time.now('+1d')}, ${time.now('-2h')} - Custom functions: ${fn.uuid()},
 * ${fn.random(1, 100)} - Variable cross-references with dependency resolution
 */
@Slf4j
public class VariableEngine {

    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final Map<String, TestifyFunction> functionRegistry = new HashMap<>();
    private final ParserContext templateContext = ParserContext.TEMPLATE_EXPRESSION;

    public VariableEngine(List<TestifyFunction> customFunctions) {
        // 2. 注册用户自定义函数 (由 Spring 注入)
        if (customFunctions != null) {
            customFunctions.forEach(f -> functionRegistry.put(f.getPrefix(), f));
        }
    }

    // --- 1. Entry Points (入口方法) ---
    public Map<String, Object> resolveVariables(Map<String, String> rawVariables) {
        Map<String, Object> context = new LinkedHashMap<>();
        if (rawVariables == null) return context;

        int limit = 10;
        while (limit-- > 0) {
            boolean changed = false;
            for (Map.Entry<String, String> entry : rawVariables.entrySet()) {
                String key = entry.getKey();
                if (context.containsKey(key)) continue;

                Object evaluated = evaluate(entry.getValue(), context);
                // 只有解析结果不再是字符串，或者字符串中不含占位符，才算真正解析完成
                if (!(evaluated instanceof String s) || !s.contains("${")) {
                    context.put(key, evaluated);
                    changed = true;
                }
            }
            if (!changed) break;
        }
        return context;
    }

    // --- 2. Core Evaluator (评估核心) ---

    // 修改后的 evaluate 核心逻辑
    public Object evaluate(String text, Map<String, Object> context) {
        if (text == null || !text.contains("${")) return text;

        StandardEvaluationContext spelContext = buildSpelContext(context);
        String trimmed = text.trim();

        // 场景 A: 纯占位符 "${faker.name()}"
        if (trimmed.startsWith("${") && trimmed.endsWith("}") && countOccurrences(trimmed, "${") == 1) {
            return resolveSinglePlaceholder(trimmed, context);
        }

        // 场景 B: 混合字符串 "User: ${user.name}"
        // 场景 B: 混合字符串 "User: ${user}, Age: ${age}" -> 局部替换逻辑
        return resolveMixedText(text, context);
    }
    /**
     * 处理纯占位符：支持返回 Object 类型
     */
    private Object resolveSinglePlaceholder(String placeholder, Map<String, Object> context) {
        String expression = placeholder.substring(2, placeholder.length() - 1);
        String spelExpr = expression.startsWith("#") ? expression : "#" + expression;
        try {
            StandardEvaluationContext spelContext = buildSpelContext(context);
            Object value = spelParser.parseExpression(spelExpr).getValue(spelContext);
            return (value != null) ? value : placeholder; // 找不到返回原始 "${xxx}"
        } catch (Exception e) {
            return placeholder;
        }
    }
    /**
     * 局部替换：仅替换存在的变量，不存在的保留
     */
    private String resolveMixedText(String text, Map<String, Object> context) {
        // 匹配 ${...}，使用非贪婪匹配
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        int lastCursor = 0;

        while (matcher.find()) {
            // 放入占位符之前的普通文本
            sb.append(text, lastCursor, matcher.start());

            String placeholder = matcher.group(0); // 完整的 ${xxx}
            Object resolved = resolveSinglePlaceholder(placeholder, context);

            // 将解析结果拼接到结果中
            sb.append(resolved != null ? resolved.toString() : placeholder);

            lastCursor = matcher.end();
        }
        sb.append(text.substring(lastCursor));
        return sb.toString();
    }

    // --- 3. Helpers (内部辅助) ---

    private StandardEvaluationContext buildSpelContext(Map<String, Object> context) {
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.addPropertyAccessor(new MapAccessor());

        // 注册核心工具
        functionRegistry.forEach((prefix, functionInstance) -> {
            ctx.setVariable(prefix, functionInstance);
        });
        ctx.setVariable("faker", new Faker());
        // 注册业务变量
        if (context != null) {
            context.forEach(ctx::setVariable);
        }
        return ctx;
    }

    private String ensureHashPrefix(String text) {
        // 匹配 ${ 后紧跟字母或数字的情况（变量名），补上 #
        // 排除 ${' (字符串), ${# (已补), ${- (负数) 等
        return text.replaceAll("#\\{(?![#0-9'\"\\-])", "#{#");
    }

    private int countOccurrences(String str, String sub) {
        return (str.length() - str.replace(sub, "").length()) / sub.length();
    }

    /**
     * 统一递归解析入口：支持 JsonNode, Map, Collection, String
     */
    public Object resolveValue(Object value, Map<String, Object> context) {
        if (value == null) return null;

        // 1. 处理 JsonNode (来自 Jackson 解析的原始结构)
        if (value instanceof JsonNode node) {
            if (node.isTextual()) {
                return evaluate(node.asText(), context);
            }
            if (node.isNumber()) return node.numberValue(); // 保持数字类型
            if (node.isBoolean()) return node.booleanValue(); // 保持布尔类型
            if (node.isNull()) return null;
            if (node.isObject()) {
                Map<String, Object> resolvedMap = new LinkedHashMap<>();
                node.fields().forEachRemaining(entry -> {
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
                    // 数组中是否过滤 null 取决于需求，通常建议保留以维持索引对齐，
                    // 或者根据配置过滤。这里演示不过滤数组内部的 null。
                    resolvedList.add(resolved);
                });
                return resolvedList;
            }
            return JsonUtil.convertValue(node, Object.class);
        }

        // 2. 处理标准 Java 字符串
        if (value instanceof String str) {
            return evaluate(str, context);
        }

        // 3. 处理 Map (支持递归处理嵌套对象)
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

        // 4. 处理集合
        if (value instanceof Collection<?> col) {
            return col.stream()
                    .map(i -> resolveValue(i, context))
                    .filter(Objects::nonNull)
                    .toList();
        }

        return value;
    }
}
