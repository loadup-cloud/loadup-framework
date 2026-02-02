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
import lombok.Getter;
import lombok.Setter;

/**
 * @author lise
 * @version TestContext.java, v 0.1 2026年01月13日 10:34 lise
 */
// 对应 YAML 的根结构
@Getter
@Setter
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
}
