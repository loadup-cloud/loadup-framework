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
import io.github.loadup.testify.asserts.operator.impl.ApproxTimeMatcher;
import io.github.loadup.testify.asserts.operator.impl.JsonMatcher;
import io.github.loadup.testify.asserts.operator.impl.ListMatcher;
import io.github.loadup.testify.asserts.operator.impl.NumberMatcher;
import io.github.loadup.testify.asserts.operator.impl.RegexMatcher;
import io.github.loadup.testify.asserts.operator.impl.SimpleMatcher;
import io.github.loadup.testify.asserts.operator.impl.StringMatcher;
import java.util.List;
import java.util.Map;

public class OperatorProcessor {

    // 建议通过 Spring 自动注入所有的 Matcher 实现类
    private static final List<OperatorMatcher> MATCHERS = List.of(
            new NumberMatcher(),
            new RegexMatcher(),
            new ApproxTimeMatcher(),
            new JsonMatcher(),
            new StringMatcher(), // 包含 contains, ne 等
            new ListMatcher());

    public static MatchResult process(Object actual, Object expected) {
        // 1. 如果不是 Map，走最简单的等值比对
        if (!(expected instanceof Map<?, ?> config)) {
            return SimpleMatcher.eq(actual, expected);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> operatorConfig = (Map<String, Object>) config;
        String op = String.valueOf(operatorConfig.getOrDefault("op", "eq"));
        Object val = operatorConfig.get("val");

        // 2. 寻找合适的 Matcher 并执行
        return MATCHERS.stream()
                .filter(m -> m.support(op))
                .findFirst()
                .map(m -> m.match(actual, val, operatorConfig))
                // 3. 兜底逻辑：如果找不到算子，尝试 eq
                .orElseGet(() -> SimpleMatcher.eq(actual, val));
    }
}
