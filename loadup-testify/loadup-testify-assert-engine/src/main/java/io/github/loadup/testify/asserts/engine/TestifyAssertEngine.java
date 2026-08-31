package io.github.loadup.testify.asserts.engine;

/*-
 * #%L
 * Testify Assert Engine
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

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public interface TestifyAssertEngine {
    /**
     * 执行断言逻辑
     *
     * @param expectNode 对应的 YAML 子节点内容
     * @param actual     实际值（由编排器传入）
     * @param context    testcontext
     */
    void compare(JsonNode expectNode, Object actual, Map<String, Object> context);
}
