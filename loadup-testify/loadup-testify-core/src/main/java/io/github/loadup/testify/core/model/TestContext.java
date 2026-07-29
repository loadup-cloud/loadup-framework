package io.github.loadup.testify.core.model;

/*-
 * #%L
 * Testify Core
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
import java.util.List;
import java.util.Map;

/**
 * @author lise
 * @version TestContext.java, v 0.1 2026年01月13日 10:34 lise
 */
// 对应 YAML 的根结构
public final class TestContext {
    private String testName;
    private String yamlPath;
    private Map<String, Object> variables;
    private JsonNode input;
    private List<MockConfig> mocks;
    private JsonNode setup;
    private JsonNode expect;

    public String testName() {
        return testName;
    }

    public String yamlPath() {
        return yamlPath;
    }

    public Map<String, Object> variables() {
        return variables;
    }

    public JsonNode input() {
        return input;
    }

    public List<MockConfig> mocks() {
        return mocks;
    }

    public JsonNode setup() {
        return setup;
    }

    public JsonNode expect() {
        return expect;
    }

    public String getTestName() {
        return this.testName;
    }

    public String getYamlPath() {
        return this.yamlPath;
    }

    public Map<String, Object> getVariables() {
        return this.variables;
    }

    public JsonNode getInput() {
        return this.input;
    }

    public List<MockConfig> getMocks() {
        return this.mocks;
    }

    public JsonNode getSetup() {
        return this.setup;
    }

    public JsonNode getExpect() {
        return this.expect;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setYamlPath(String yamlPath) {
        this.yamlPath = yamlPath;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public void setInput(JsonNode input) {
        this.input = input;
    }

    public void setMocks(List<MockConfig> mocks) {
        this.mocks = mocks;
    }

    public void setSetup(JsonNode setup) {
        this.setup = setup;
    }

    public void setExpect(JsonNode expect) {
        this.expect = expect;
    }
}
