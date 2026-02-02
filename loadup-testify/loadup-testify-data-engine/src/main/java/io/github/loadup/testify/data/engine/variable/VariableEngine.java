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
import com.fasterxml.jackson.databind.node.*;
import io.github.loadup.testify.core.util.JsonUtil;
import io.github.loadup.testify.data.engine.function.TestifyFunction;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

/**
 * Variable resolution engine supporting: - Datafaker expressions: ${faker.name.firstName} - Time
 * functions with offsets: ${time.now('+1d')}, ${time.now('-2h')} - Custom functions: ${fn.uuid()},
 * ${fn.random(1, 100)} - Variable cross-references with dependency resolution
 */
@Slf4j
public class VariableEngine {

    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final Map<String, TestifyFunction> functionRegistry = new HashMap<>();
    // 匹配 ${prefix.method(args)}
    private final ParserContext templateContext = new ParserContext() {
        @Override
        public boolean isTemplate() {
            return true;
        }

        @Override
        public String getExpressionPrefix() {
            return "${";
        }

        @Override
        public String getExpressionSuffix() {
            return "}";
        }
    };

    public VariableEngine(List<TestifyFunction> customFunctions) {
        // 2. 注册用户自定义函数 (由 Spring 注入)
        if (customFunctions != null) {
            customFunctions.forEach(f -> functionRegistry.put(f.getPrefix(), f));
        }
    }

    /** 解析入口，假设已经正则匹配出: "time", "afterDays", ["5"] */
    public Object executeFunction(String prefix, String methodName, String argsStr) {
        TestifyFunction target = functionRegistry.get(prefix);
        if (target == null) throw new RuntimeException("Unknown function prefix: " + prefix);
        // 解析参数列表 (简单处理：按逗号分隔，并去除空格)
        String[] rawArgs = StringUtils.hasText(argsStr) ? argsStr.split(",") : new String[0];
        // 1. 找到匹配的方法
        Method method = Arrays.stream(target.getClass().getMethods())
                .filter(m -> m.getName().equals(methodName) && m.getParameterCount() == rawArgs.length)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Method not found: " + methodName));

        try {
            // 2. 动态转换参数类型 (String -> target method type)
            Object[] convertedArgs = new Object[method.getParameterCount()];
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < rawArgs.length; i++) {
                // 利用 Jackson 的强力转换能力，支持基本类型、List、Map 等转换
                String arg = rawArgs[i].trim().replaceAll("^['\"]|['\"]$", ""); // 去掉可能的引号
                convertedArgs[i] = JsonUtil.convertValue(arg, parameterTypes[i]);
            }

            // 3. 反射调用
            return method.invoke(target, convertedArgs);
        } catch (Exception e) {
            throw new RuntimeException("Function execution failed: " + methodName, e);
        }
    }

    // --- 1. Entry Points (入口方法) ---
    public Map<String, Object> resolveVariables(Map<String, String> rawVariables) {
        Map<String, Object> context = new LinkedHashMap<>();
        if (rawVariables == null) return context;

        // 拓扑排序的简化版：迭代尝试
        // 假设 a: ${b}, b: 1。第一轮 a 失败, b 成功；第二轮 a 成功。
        int maxIterations = 3;
        for (int i = 0; i < maxIterations; i++) {
            boolean changed = false;
            for (Map.Entry<String, String> entry : rawVariables.entrySet()) {
                String key = entry.getKey();
                if (context.containsKey(key)) continue;

                try {
                    Object evaluated = evaluate(entry.getValue(), context);
                    // 只有当解析结果不再包含 ${ 占位符时，才认为该变量已完全解析
                    if (evaluated != null && !String.valueOf(evaluated).contains("${")) {
                        context.put(key, evaluated);
                        changed = true;
                    }
                } catch (Exception ignored) {
                }
            }
            if (!changed) break; // 如果这一轮没有新变量被解析，提前退出
        }
        return context;
    }

    // --- 2. Core Evaluator (评估核心) ---

    // 修改后的 evaluate 核心逻辑
    public Object evaluate(String text, Map<String, Object> context) {
        if (text == null || !text.contains("${")) return text;

        try {
            StandardEvaluationContext spelContext = buildSpelContext(context);
            String trimmed = text.trim();

            // 场景 A: 纯占位符 "${faker.name()}"
            if (trimmed.startsWith("${") && trimmed.endsWith("}") && countOccurrences(trimmed, "${") == 1) {
                String expression = trimmed.substring(2, trimmed.length() - 1);

                // --- 修复点：针对纯表达式，手动补一个 # ---
                String spelExpr = expression.startsWith("#") ? expression : "#" + expression;

                return spelParser.parseExpression(spelExpr).getValue(spelContext);
            }

            // 场景 B: 混合字符串 "Hello ${faker.name()}"
            // 这里 ensureHashPrefix 会把 "${" 替换成 "${#"
            String processedText = ensureHashPrefix(text);
            return spelParser.parseExpression(processedText, templateContext).getValue(spelContext);

        } catch (Exception e) {
            log.warn("SpEL 解析失败: {}, 错误: {}", text, e.getMessage());
            return text;
        }
    }

    /** 场景 B: 解析整个 JsonNode 树 (用于 input, setup, expect 等) */
    public JsonNode resolveJsonNode(JsonNode node, Map<String, Object> context) {
        if (node == null || !node.isContainerNode() && !node.isTextual()) return node;

        if (node.isTextual()) {
            return convertToNode(evaluate(node.asText(), context));
        }

        if (node.isObject()) {
            ObjectNode newNode = JsonNodeFactory.instance.objectNode();
            node.fields()
                    .forEachRemaining(entry -> newNode.set(entry.getKey(), resolveJsonNode(entry.getValue(), context)));
            return newNode;
        }

        if (node.isArray()) {
            ArrayNode newNode = JsonNodeFactory.instance.arrayNode();
            node.forEach(item -> newNode.add(resolveJsonNode(item, context)));
            return newNode;
        }
        return node;
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
        // 匹配 ${ 后面不是 # 的所有情况，自动补上 #。使得 ${time.now()} 变为 ${#time.now()}
        return text.replaceAll("\\$\\{(?![#0-9'\"\\-])", "\\${#");
    }

    private JsonNode convertToNode(Object obj) {
        if (obj == null) return NullNode.instance;
        return switch (obj) {
            case JsonNode j -> j;
            case Integer i -> IntNode.valueOf(i);
            case Long l -> LongNode.valueOf(l);
            case Double d -> DoubleNode.valueOf(d);
            case Boolean b -> BooleanNode.valueOf(b);
            case TemporalAccessor t ->
                TextNode.valueOf(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(t));
            default -> TextNode.valueOf(obj.toString());
        };
    }

    private int countOccurrences(String str, String sub) {
        return (str.length() - str.replace(sub, "").length()) / sub.length();
    }

    // 建议添加到 VariableEngine 类中
    public Object resolveValue(Object value, Map<String, Object> context) {
        if (value == null) return null;

        // 1. 如果是 JsonNode (来自 YAML 解析后的原始结构)
        if (value instanceof JsonNode node) {
            // 调用我们之前实现的递归解析 JsonNode 的方法
            JsonNode resolvedNode = resolveJsonNode(node, context);
            // 将解析后的 JsonNode 转回 Java 对象（Map, List, 或基本类型）
            return JsonUtil.convertValue(resolvedNode, Object.class);
        }

        // 2. 如果是 字符串 (比如 "Welcome ${name}")
        if (value instanceof String str) {
            return evaluate(str, context);
        }

        // 3. 如果是 集合 (手动创建的 List/Map)
        if (value instanceof Collection<?> col) {
            return col.stream().map(item -> resolveValue(item, context)).toList();
        }

        // 2. 处理 Map 嵌套 (YAML 中的复杂对象)
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            Map<String, Object> resolvedMap = new LinkedHashMap<>(map.size());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                resolvedMap.put(entry.getKey(), resolveValue(entry.getValue(), context)); // 递归调用
            }
            return resolvedMap;
        }

        return value;
    }
}
