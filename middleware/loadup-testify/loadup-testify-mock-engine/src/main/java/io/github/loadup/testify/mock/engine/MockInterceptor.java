package io.github.loadup.testify.mock.engine;

/*-
 * #%L
 * Testify Mock Engine
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

import com.jayway.jsonpath.JsonPath;
import io.github.loadup.testify.core.model.MockRule;
import io.github.loadup.testify.core.util.JsonUtil;
import io.github.loadup.testify.data.engine.variable.VariableEngine;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/** 核心拦截器：实现 AOP 方法拦截、多规则参数匹配及动态变量解析 */
@Slf4j
public class MockInterceptor implements MethodInterceptor {

    private final VariableEngine variableEngine;
    // 注入 Spring 的转换服务
    private final ConversionService conversionService = DefaultConversionService.getSharedInstance();

    /** 存储结构：Map<BeanName, Map<MethodName, List<MockRule>>> 一个方法可以对应多个 Rule（不同参数对应不同返回值） */
    @Getter
    private final Map<String, Map<String, List<MockRule>>> mockRules = new ConcurrentHashMap<>();

    public MockInterceptor(VariableEngine variableEngine) {
        this.variableEngine = variableEngine;
    }

    /** 注册 Mock 规则 */
    public void registerMock(
            String beanName,
            String methodName,
            List<Object> expectedArgs,
            Object returnValue,
            Throwable throwEx,
            Class<?> returnType,
            Map<String, Object> context,
            Long delay) {

        mockRules
                .computeIfAbsent(beanName, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(methodName, k -> new ArrayList<>())
                .add(new MockRule(expectedArgs, returnValue, throwEx, returnType, context, delay, false));

        log.info(
                ">>> [TESTIFY-REGISTRY] Registered Mock: {}.{} (Args: {})",
                beanName,
                methodName,
                expectedArgs == null ? "ANY" : expectedArgs);
    }

    public void clear() {
        mockRules.clear();
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // 快速路径：无规则直接放行
        if (mockRules.isEmpty()) {
            return invocation.proceed();
        }

        String beanName = resolveBeanName(invocation.getThis());
        String methodName = invocation.getMethod().getName();

        Map<String, List<MockRule>> beanRules = mockRules.get(beanName);
        if (beanRules != null) {
            List<MockRule> rules = beanRules.get(methodName);
            if (rules != null) {
                // 遍历规则，寻找匹配当前入参的 Rule
                for (MockRule rule : rules) {
                    if (isArgsMatch(rule, invocation)) {
                        rule.setHit(true); // 标记命中
                        return executeMockAction(rule);
                    }
                }
            }
        }

        // 未命中任何 Mock 规则，执行原始逻辑（或交给开发人员手动 Mockito 处理）
        return invocation.proceed();
    }

    /** 参数匹配核心算法 */

    // 修改后的匹配方法
    private boolean isArgsMatch(MockRule rule, MethodInvocation invocation) {
        Object[] actualArgs = invocation.getArguments();
        Method method = invocation.getMethod();
        List<Object> rawExpectedArgs = rule.getExpectedArgs();

        // 1. YAML 没配 args，代表匹配任意参数
        if (rawExpectedArgs == null) {
            return true;
        }

        // 2. 参数数量不等
        if (rawExpectedArgs.size() != actualArgs.length) {
            return false;
        }

        // 3. 【核心修复】解析预期参数中的变量
        // 例如将 ["${userId}"] 解析为 [123]
        List<Object> resolvedExpectedArgs =
                (List<Object>) variableEngine.resolveValue(rawExpectedArgs, rule.getContext());

        // 4. 逐个比对
        for (int i = 0; i < resolvedExpectedArgs.size(); i++) {
            Object expected = resolvedExpectedArgs.get(i);
            Object actual = actualArgs[i];

            if ("any".equals(expected)) {
                continue;
            }
            if (expected == null && actual == null) {
                continue;
            }
            // 识别 JSONPath 模式
            if (expected instanceof String && ((String) expected).startsWith("$jsonPath")) {
                return matchJsonPath((String) expected, actual);
            }
            // 获取该位置参数的真实定义类型（例如 Long.class）
            Class<?> targetType = method.getParameterTypes()[i];

            try {
                // 核心：将 YAML 解析出的值强转为方法参数的真实类型再比对
                Object convertedExpected = conversionService.convert(expected, targetType);

                if (convertedExpected != null && !convertedExpected.equals(actual)) {
                    // 如果 equals 失败，再走 JSON 兜底（处理 Map/POJO 比对）
                    if (!JsonUtil.equals(convertedExpected, actual)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                // 如果类型转换失败（比如字符串转数字转不动），降级走 JSON 比对
                if (!JsonUtil.equals(expected, actual)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** 执行 Mock 结果：解析变量并处理类型转换 */
    private Object executeMockAction(MockRule rule) throws Throwable {
        // 1. 处理延迟模拟
        if (rule.getDelay() != null && rule.getDelay() > 0) {
            log.info(">>> [TESTIFY-LATENCY] Simulating delay: {}ms", rule.getDelay());
            try {
                Thread.sleep(rule.getDelay());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // A. 异常 Mock
        if (Objects.nonNull(rule.getThrowEx())) {
            log.info(">>> [TESTIFY-HIT] Mock Throwing: {}", rule.getThrowEx().getCause());
            throw rule.getThrowEx();
        }

        // B. 返回值 Mock
        Object rawReturn = rule.getReturnValue();
        // 关键：调用 VariableEngine 执行动态解析（支持 now() 等函数）
        Object resolvedValue = variableEngine.resolveValue(rawReturn, rule.getContext());

        // 类型强制转换：确保从 Map/String 转换为方法要求的真实类型
        if (resolvedValue != null
                && rule.getReturnType() != null
                && !rule.getReturnType().isAssignableFrom(resolvedValue.getClass())) {

            log.debug(
                    ">>> [TESTIFY-AOP] Converting result to type: {}",
                    rule.getReturnType().getName());
            resolvedValue = JsonUtil.convertValue(resolvedValue, rule.getReturnType());
        }

        log.info(">>> [TESTIFY-HIT] Mock Returning: {}", resolvedValue);
        return resolvedValue;
    }

    private String resolveBeanName(Object instance) {
        // 穿透代理获取原始类名并转为首字母小写
        Class<?> targetClass = AopUtils.getTargetClass(instance);
        String simpleName = targetClass.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    /**
     * 执行 JSONPath 匹配
     *
     * @param expression 形如 "$jsonPath($.id): 123"
     * @param actual 运行时真实的入参对象
     */
    private boolean matchJsonPath(String expression, Object actual) {
        try {
            // 解析表达式，提取路径和期望值
            // 简单处理：截取 () 里的路径和 : 后的期望值
            String path = expression.substring(expression.indexOf("(") + 1, expression.indexOf(")"));
            String expectedValueStr =
                    expression.substring(expression.indexOf(":") + 1).trim();

            // 将实际对象转为 JSON 字符串或 Document
            String actualJson = JsonUtil.toJson(actual);
            if (!expression.contains(":")) {
                path = expression.substring(expression.indexOf("(") + 1, expression.lastIndexOf(")"));
                Object result = JsonPath.read(actualJson, path);
                // 对于过滤谓词，如果匹配到数据，返回的是非空 List
                if (result instanceof List) {
                    return !((List<?>) result).isEmpty();
                }
                return result != null;
            }
            Object actualValue = JsonPath.read(actualJson, path);

            // 比对提取出的值与期望值（统一转字符串比对，避开类型坑）
            return String.valueOf(actualValue).equals(expectedValueStr);
        } catch (Exception e) {
            log.warn(">>> [TESTIFY-JSONPATH] 匹配失败: {} , 异常: {}", expression, e.getMessage());
            return false;
        }
    }
}
