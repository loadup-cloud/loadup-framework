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

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

public interface TestifyAssertEngine {
    /** 该引擎负责处理的 YAML 节点名称，如 "response", "database", "exception" */
    String supportKey();

    /**
     * 执行断言逻辑
     *
     * @param expectNode 对应的 YAML 子节点内容
     * @param actual 实际值（由编排器传入）
     * @param context testcontext
     * @param reportList 错误报表收集器
     */
    void compare(JsonNode expectNode, Object actual, Map<String, Object> context, List<String> reportList);

    // 在 TestifyAssertEngine 接口中增加默认转换逻辑

}
