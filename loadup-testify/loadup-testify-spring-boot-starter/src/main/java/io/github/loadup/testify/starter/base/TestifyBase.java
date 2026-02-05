package io.github.loadup.testify.starter.base;

/*-
 * #%L
 * Testify Spring Boot Starter
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
import io.github.loadup.testify.asserts.facade.AssertionFacade;
import io.github.loadup.testify.core.annotation.TestifyParam;
import io.github.loadup.testify.core.model.TestContext;
import io.github.loadup.testify.core.util.JsonUtil;
import io.github.loadup.testify.core.variable.VariableContext;
import io.github.loadup.testify.data.engine.db.SqlExecutionEngine;
import io.github.loadup.testify.data.engine.variable.VariableEngine;
import io.github.loadup.testify.mock.engine.MockEngine;
import io.github.loadup.components.testcontainers.initializer.TestContainersContextInitializer;
import io.github.loadup.testify.starter.loader.YamlLoader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

@Slf4j
@SpringBootTest
@ContextConfiguration(initializers = TestContainersContextInitializer.class)
public abstract class TestifyBase extends AbstractTestNGSpringContextTests {

    @Autowired
    protected AssertionFacade assertionFacade;

    @Autowired
    protected SqlExecutionEngine sqlExecutionEngine;

    @Autowired
    protected MockEngine mockEngine;

    @Autowired
    protected VariableEngine variableEngine;

    private TestContext currentTestContext;
    private final YamlLoader yamlLoader = new YamlLoader();

    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @BeforeMethod
    public void prepareContext(Method method, ITestContext testngContext) {
        String contextKey = getContextKey(method);
        this.currentTestContext = (TestContext) testngContext.getAttribute(contextKey);
    }

    @DataProvider(name = "testifyData")
    public Iterator<Object[]> provideTestData(ITestContext testngContext, Method method) {
        try {

            // 1. 加载测试上下文
            TestContext testContext = loadTestContext(method);

            // 2. 解析变量并存入上下文
            Map<String, Object> resolvedVars = resolveVariables(testContext);
            VariableContext.set(resolvedVars);

            // 3. 存储到TestNG上下文供后续使用
            storeTestContext(testngContext, method, testContext);

            // 4. 转换输入参数
            // 转换为 Map<String, Object>
            // 使用 TypeReference 来确保泛型信息不丢失（尤其是处理嵌套结构时）
            Map<String, Object> inputMap = loadInputFromYaml(testContext.input());
            List<Object[]> dataList = convertInputToArguments(inputMap, method);

            return dataList.iterator();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Testify DataProvider failed for " + method.getName() + ": " + e.getMessage(), e);
        }
    }

    private Map<String, Object> loadInputFromYaml(JsonNode inputNode) {
        // 1. 获取当前测试上下文中的 JsonNode

        if (inputNode == null || inputNode.isMissingNode() || inputNode.isNull()) {
            return Collections.emptyMap();
        }

        // 2. 转换为有序 Map
        try {
            if (inputNode.isArray()) {
                // 如果是数组，可以按索引转换成 Map (key 为 "arg0", "arg1" 等)
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < inputNode.size(); i++) {
                    map.put("arg" + i, inputNode.get(i));
                }
                return map;
            } else {
                // 如果是对象，直接转换
                return JsonUtil.convertValue(inputNode, new TypeReference<LinkedHashMap<String, Object>>() {});
            }

        } catch (IllegalArgumentException e) {
            log.error(">>> [TESTIFY] Failed to convert JsonNode to Map: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    // ================ 私有辅助方法 ================

    /** 加载测试上下文 */
    private TestContext loadTestContext(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String yamlPath = String.format("testcases/%s/%s.yaml", className, methodName);

        TestContext testContext = yamlLoader.load(yamlPath);
        if (testContext == null) {
            throw new RuntimeException("Cannot find YAML file at: " + yamlPath);
        }
        return testContext;
    }

    /** 解析变量 */
    private Map<String, Object> resolveVariables(TestContext testContext) {
        Map<String, String> rawVars = testContext.variables() != null
                ? JsonUtil.convertValue(testContext.variables(), new TypeReference<>() {})
                : Collections.emptyMap();
        return variableEngine.resolveVariables(rawVars);
    }

    /** 存储测试上下文到TestNG */
    private void storeTestContext(ITestContext testngContext, Method method, TestContext testContext) {
        String contextKey = getContextKey(method);
        testngContext.setAttribute(contextKey, testContext);
    }

    /** 获取上下文键 */
    private String getContextKey(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    /** 转换输入参数 */
    private List<Object[]> convertInputToArguments(Map<String, Object> inputNode, Method method) {
        List<Object[]> dataList = new ArrayList<>();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Parameter[] parameters = method.getParameters();
        // 尝试获取编译器保存的参数名 (Spring 6+ 需要开启 -parameters)
        String[] discoveredNames = nameDiscoverer.getParameterNames(method);

        List<Object> yamlValues = new ArrayList<>(inputNode.values());
        if (inputNode == null || inputNode.isEmpty()) {
            // 无参数情况
            dataList.add(new Object[parameterTypes.length]);
            return dataList;
        }

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            String targetKey = null;

            // --- 策略 A: 检查是否有 @TestifyParam 注解 (优先级最高) ---
            if (param.isAnnotationPresent(TestifyParam.class)) {
                targetKey = param.getAnnotation(TestifyParam.class).value();
            }

            // --- 策略 B: 尝试通过反射名称匹配 ---
            if (targetKey == null && discoveredNames != null && i < discoveredNames.length) {
                String dName = discoveredNames[i];
                // 排除混淆后的 arg0, arg1 这种无意义名称
                if (!dName.startsWith("arg") && inputNode.containsKey(dName)) {
                    targetKey = dName;
                }
            }
            Object rawValue;
            if (targetKey != null && inputNode.containsKey(targetKey)) {
                rawValue = inputNode.get(targetKey);
                log.debug(">>> [TESTIFY] Param [{}] matched by key: {}", i, targetKey);
            } else {
                // --- 策略 C: 顺序索引兜底 ---
                rawValue = (i < yamlValues.size()) ? yamlValues.get(i) : null;
                log.warn(
                        ">>> [TESTIFY] Param [{}] fallback to sequential index matching. "
                                + "Please enable '-parameters' for better clarity.",
                        i);
            }
            // 4. 递归解析变量 + Jackson 强转 POJO
            dataList.add(new Object[] {resolveAndConvert(rawValue, param.getType())});
        }

        return dataList;
    }

    private Object resolveAndConvert(Object raw, Class<?> targetType) {
        // 递归处理 ${userName}, ${dynamicValue} 等变量替换
        Map<String, Object> vars = VariableContext.get();
        Object resolved = variableEngine.resolveValue(raw, vars);
        // 将 Map/List 转换为目标 POJO (User 对象、String 等)
        return JsonUtil.convertValue(resolved, targetType);
    }

    /** 核心编排方法 */
    protected <T> void runTest(Supplier<T> action) {
        try {
            Map<String, Object> resolvedVars = VariableContext.get();

            // --- 阶段 A: 环境准备 ---
            prepareTestEnvironment(resolvedVars);

            // --- 阶段 B: 业务执行 ---
            T actualResult = null;
            Throwable businessError = null;
            try {
                actualResult = action.get();
            } catch (Throwable e) {
                businessError = e;
                logger.error(e);
            }

            // --- 阶段 C: 断言 ---
            List<String> reports = executeAssertions(actualResult, businessError);

            // --- 阶段 D: 结果处理 ---
            processReports(reports);

        } catch (Exception e) {
            log.error("Testify execution failed", e);
            throw new RuntimeException("Testify execution error", e);
        } finally {
            cleanup();
        }
    }

    /** 准备测试环境 */
    private void prepareTestEnvironment(Map<String, Object> resolvedVars) {
        log.info("Resolved Variables: {}", resolvedVars);

        // 应用Mock配置
        if (currentTestContext.mocks() != null) {
            mockEngine.applyMocks(currentTestContext.mocks(), resolvedVars);
        }

        // 执行Setup SQL
        if (currentTestContext.setup() != null) {
            sqlExecutionEngine.execute(currentTestContext.setup(), resolvedVars, currentTestContext.yamlPath());
        }
    }

    /** 执行断言 */
    private List<String> executeAssertions(Object actualResult, Throwable businessError) {
        return assertionFacade.executeAll(currentTestContext.expect(), actualResult, businessError);
    }

    /** 清理资源 */
    private void cleanup() {
        VariableContext.clear();
        // 如果需要重置Mock，可以在这里添加
        mockEngine.resetAllMocks();
    }

    private void processReports(List<String> reports) {
        if (reports == null || reports.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder("\n--- Testify Assertion Reports ---\n");
        boolean hasFailure = false;
        for (String report : reports) {
            sb.append(report).append("\n");
            if (report.contains("❌")) {
                hasFailure = true;
            }
        }

        String finalReport = sb.toString();
        Reporter.log(finalReport);

        if (hasFailure) {
            throw new AssertionError(finalReport);
        }
    }

    /** 提供一个静态便捷方法给业务代码使用，尽量缩短代码长度 */
    @SuppressWarnings("unchecked")
    protected <V> V val(V raw) {
        if (raw == null) return null;

        // 从ThreadLocal拿到已经解析好的variables映射表
        Map<String, Object> currentContext = VariableContext.get();

        if (raw instanceof String str) {
            // 调用evaluate。此时如果str是"${userId}"，且variables里定义了userId，
            // evaluate会优先从currentContext中获取。
            return (V) variableEngine.evaluate(str, currentContext);
        }
        return raw;
    }
}
