package io.github.loadup.testify.starter.config;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.testify")
public class TestifyProperties {
    /**
     * Testcontainers 基础设施配置
     */
    private TestcontainersProperties testcontainers = new TestcontainersProperties();

    /**
     * 全局变量池
     */
    private Map<String, Object> variables = new LinkedHashMap<>();

    /**
     * 测试输入参数，Key 为方法参数名或索引
     */
    private Map<String, Object> input = new LinkedHashMap<>();

    /**
     * 前置脚本或数据准备
     */
    private Map<String, Object> setup = new LinkedHashMap<>();

    /**
     * Mock 规则定义
     */
    private List<MockDefinition> mocks = new ArrayList<>();

    /**
     * 预期结果定义
     */
    private ExpectDefinition expect = new ExpectDefinition();

    public static class TestcontainersProperties {
        /**
         * 是否启用 Testcontainers 自动化管理
         */
        private boolean enabled = false;

        /**
         * 是否开启全局容器复用 (需配合 ~/.testcontainers.properties)
         */
        private boolean reuse = false;

        /**
         * 基础设施服务列表 (MySQL, Redis, Kafka, etc.)
         */
        private List<ServiceConfig> services = new ArrayList<>();
    }

    public static class ServiceConfig {
        /**
         * 服务类型: mysql, redis, kafka, mongodb, postgresql, elasticsearch, localstack
         */
        private String type;

        /**
         * 容器镜像地址 (可选，不填则使用 Provider 默认值)
         */
        private String image;

        /**
         * 初始化脚本路径 (主要针对 DB 类型)
         */
        private String initScript;

        /**
         * 数据库名称 (针对 MySQL/Postgres)
         */
        private String database;

        /**
         * 针对 Localstack 的子服务列表 (s3, sqs, etc.)
         */
        private List<String> subServices = new ArrayList<>();

        /**
         * 其他自定义扩展配置
         */
        private Map<String, Object> options = new HashMap<>();
    }

    // --- 内部结构类 ---

    public static class MockDefinition {
        private String bean;
        private String method;
        private Map<String, Object> args = new LinkedHashMap<>();
        private Object thenReturn;
        private ExceptionDefinition thenThrow;
    }

    public static class ExpectDefinition {
        private Object response; // 支持 Map 或具体 POJO
        private ExceptionDefinition exception;
        private List<DatabaseExpectation> database = new ArrayList<>();
    }

    public static class ExceptionDefinition {
        private String type;
        private String message;
    }

    public static class DatabaseExpectation {
        private String table;
        private String mode = "strict"; // 默认严格匹配
        private List<Map<String, Object>> rows = new ArrayList<>();
    }

    public TestifyProperties(TestcontainersProperties testcontainers, Map<String, Object> variables, Map<String, Object> input, Map<String, Object> setup, List<MockDefinition> mocks, ExpectDefinition expect, boolean enabled, boolean reuse, List<ServiceConfig> services, String type, String image, String initScript, String database, List<String> subServices, Map<String, Object> options, String bean, String method, Map<String, Object> args, Object thenReturn, ExceptionDefinition thenThrow, Object response, ExceptionDefinition exception, List<DatabaseExpectation> database, String type, String message, String table, String mode, List<Map<String, Object>> rows) {
        this.testcontainers = testcontainers;
        this.variables = variables;
        this.input = input;
        this.setup = setup;
        this.mocks = mocks;
        this.expect = expect;
        this.enabled = enabled;
        this.reuse = reuse;
        this.services = services;
        this.type = type;
        this.image = image;
        this.initScript = initScript;
        this.database = database;
        this.subServices = subServices;
        this.options = options;
        this.bean = bean;
        this.method = method;
        this.args = args;
        this.thenReturn = thenReturn;
        this.thenThrow = thenThrow;
        this.response = response;
        this.exception = exception;
        this.database = database;
        this.type = type;
        this.message = message;
        this.table = table;
        this.mode = mode;
        this.rows = rows;
    }

    public TestifyProperties() {
    }

    public TestcontainersProperties getTestcontainers() {
        return this.testcontainers;
    }

    public Map<String, Object> getVariables() {
        return this.variables;
    }

    public Map<String, Object> getInput() {
        return this.input;
    }

    public Map<String, Object> getSetup() {
        return this.setup;
    }

    public List<MockDefinition> getMocks() {
        return this.mocks;
    }

    public ExpectDefinition getExpect() {
        return this.expect;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isReuse() {
        return this.reuse;
    }

    public List<ServiceConfig> getServices() {
        return this.services;
    }

    public String getType() {
        return this.type;
    }

    public String getImage() {
        return this.image;
    }

    public String getInitScript() {
        return this.initScript;
    }

    public String getDatabase() {
        return this.database;
    }

    public List<String> getSubServices() {
        return this.subServices;
    }

    public Map<String, Object> getOptions() {
        return this.options;
    }

    public String getBean() {
        return this.bean;
    }

    public String getMethod() {
        return this.method;
    }

    public Map<String, Object> getArgs() {
        return this.args;
    }

    public Object getThenReturn() {
        return this.thenReturn;
    }

    public ExceptionDefinition getThenThrow() {
        return this.thenThrow;
    }

    public Object getResponse() {
        return this.response;
    }

    public ExceptionDefinition getException() {
        return this.exception;
    }

    public List<DatabaseExpectation> getDatabase() {
        return this.database;
    }

    public String getType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }

    public String getTable() {
        return this.table;
    }

    public String getMode() {
        return this.mode;
    }

    public List<Map<String, Object>> getRows() {
        return this.rows;
    }

    public void setTestcontainers(TestcontainersProperties testcontainers) {
        this.testcontainers = testcontainers;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }

    public void setSetup(Map<String, Object> setup) {
        this.setup = setup;
    }

    public void setMocks(List<MockDefinition> mocks) {
        this.mocks = mocks;
    }

    public void setExpect(ExpectDefinition expect) {
        this.expect = expect;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setReuse(boolean reuse) {
        this.reuse = reuse;
    }

    public void setServices(List<ServiceConfig> services) {
        this.services = services;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setInitScript(String initScript) {
        this.initScript = initScript;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setSubServices(List<String> subServices) {
        this.subServices = subServices;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

    public void setThenReturn(Object thenReturn) {
        this.thenReturn = thenReturn;
    }

    public void setThenThrow(ExceptionDefinition thenThrow) {
        this.thenThrow = thenThrow;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public void setException(ExceptionDefinition exception) {
        this.exception = exception;
    }

    public void setDatabase(List<DatabaseExpectation> database) {
        this.database = database;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(testcontainers, variables, input, setup, mocks, expect, enabled, reuse, services, type, image, initScript, database, subServices, options, bean, method, args, thenReturn, thenThrow, response, exception, database, type, message, table, mode, rows);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestifyProperties other = (TestifyProperties) o;
        if (!java.util.Objects.equals(testcontainers, other.testcontainers)) return false;
        if (!java.util.Objects.equals(variables, other.variables)) return false;
        if (!java.util.Objects.equals(input, other.input)) return false;
        if (!java.util.Objects.equals(setup, other.setup)) return false;
        if (!java.util.Objects.equals(mocks, other.mocks)) return false;
        if (!java.util.Objects.equals(expect, other.expect)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(reuse, other.reuse)) return false;
        if (!java.util.Objects.equals(services, other.services)) return false;
        if (!java.util.Objects.equals(type, other.type)) return false;
        if (!java.util.Objects.equals(image, other.image)) return false;
        if (!java.util.Objects.equals(initScript, other.initScript)) return false;
        if (!java.util.Objects.equals(database, other.database)) return false;
        if (!java.util.Objects.equals(subServices, other.subServices)) return false;
        if (!java.util.Objects.equals(options, other.options)) return false;
        if (!java.util.Objects.equals(bean, other.bean)) return false;
        if (!java.util.Objects.equals(method, other.method)) return false;
        if (!java.util.Objects.equals(args, other.args)) return false;
        if (!java.util.Objects.equals(thenReturn, other.thenReturn)) return false;
        if (!java.util.Objects.equals(thenThrow, other.thenThrow)) return false;
        if (!java.util.Objects.equals(response, other.response)) return false;
        if (!java.util.Objects.equals(exception, other.exception)) return false;
        if (!java.util.Objects.equals(database, other.database)) return false;
        if (!java.util.Objects.equals(type, other.type)) return false;
        if (!java.util.Objects.equals(message, other.message)) return false;
        if (!java.util.Objects.equals(table, other.table)) return false;
        if (!java.util.Objects.equals(mode, other.mode)) return false;
        if (!java.util.Objects.equals(rows, other.rows)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "TestifyProperties(" + "testcontainers=" + testcontainers + ", " + "variables=" + variables + ", " + "input=" + input + ", " + "setup=" + setup + ", " + "mocks=" + mocks + ", " + "expect=" + expect + ", " + "enabled=" + enabled + ", " + "reuse=" + reuse + ", " + "services=" + services + ", " + "type=" + type + ", " + "image=" + image + ", " + "initScript=" + initScript + ", " + "database=" + database + ", " + "subServices=" + subServices + ", " + "options=" + options + ", " + "bean=" + bean + ", " + "method=" + method + ", " + "args=" + args + ", " + "thenReturn=" + thenReturn + ", " + "thenThrow=" + thenThrow + ", " + "response=" + response + ", " + "exception=" + exception + ", " + "database=" + database + ", " + "type=" + type + ", " + "message=" + message + ", " + "table=" + table + ", " + "mode=" + mode + ", " + "rows=" + rows + ")";
    }
}
