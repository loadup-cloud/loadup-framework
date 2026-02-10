package io.github.loadup.testify.asserts.operator.impl;

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

import io.github.loadup.testify.asserts.model.MatchResult;
import io.github.loadup.testify.asserts.operator.OperatorMatcher;
import io.github.loadup.testify.core.util.JsonUtil;
import java.util.Map;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/** JSON matcher supporting partial (LENIENT) and full (STRICT) comparison modes. */
public class JsonMatcher implements OperatorMatcher {

    @Override
    public boolean support(String op) {
        return "json".equals(op);
    }

    /**
     * Match JSON strings with specified comparison mode.
     *
     * @param actual Actual JSON value
     * @param expected Expected JSON value
     * @param config "full" for strict comparison, anything else for lenient (default)
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
