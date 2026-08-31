package io.github.loadup.testify.asserts.operator.impl;

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
import io.github.loadup.testify.asserts.operator.OperatorMatcher;
import io.github.loadup.testify.core.util.JsonUtil;
import java.util.Map;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * JSON matcher supporting partial (LENIENT) and full (STRICT) comparison modes.
 */
public class JsonMatcher implements OperatorMatcher {

    @Override
    public boolean support(String op) {
        return "json".equals(op);
    }

    /**
     * Match JSON strings with specified comparison mode.
     *
     * @param actual   Actual JSON value
     * @param expected Expected JSON value
     * @param config   "full" for strict comparison, anything else for lenient (default)
     * @return MatchResult
     */
    @Override
    public MatchResult match(Object actual, Object expected, Map<String, Object> config) {
        String matchMode = String.valueOf(config.getOrDefault("mode", "lenient"));
        try {
            String actStr = actual instanceof String ? (String) actual : JsonUtil.toJson(actual);
            String expStr = expected instanceof String ? (String) expected : JsonUtil.toJson(expected);

            // NON_EXTENSIBLE: 期望中有的，实际必须有且相等；实际多出的字段会被忽略（Lenient核心）
            // STRICT: 必须完全一致，字段数量、顺序（如果是数组）都要一致

            JSONCompareMode mode = "full".equals(matchMode) ? JSONCompareMode.STRICT : JSONCompareMode.LENIENT;
            JSONAssert.assertEquals(expStr, actStr, mode);
            return MatchResult.pass();
        } catch (AssertionError e) {
            // JSONAssert 报错时会提供非常详细的 Diff 信息
            return MatchResult.fail(actual, expected, e.getMessage());
        } catch (Exception e) {
            return MatchResult.fail(actual, expected, "JSON comparison error: " + e.getMessage());
        }
    }
}
