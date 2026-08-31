package io.github.loadup.testify.asserts.operator;

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

import io.github.loadup.testify.asserts.model.MatchResult;
import java.util.Map;

public interface OperatorMatcher {
    /**
     * @param actual        数据库查询到的实际值
     * @param expectedValue YAML中配置的期望对象 (如 {op: "gt", val: 100})
     * @return 匹配结果描述，如果成功返回 null，失败返回错误原因
     */
    MatchResult match(Object actual, Object expectedValue, Map<String, Object> config);

    boolean support(String op);
}
