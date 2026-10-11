/*
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
package io.github.loadup.testify.asserts.operator;

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

    private static final List<OperatorMatcher> MATCHERS = List.of(
            new NumberMatcher(),
            new RegexMatcher(),
            new ApproxTimeMatcher(),
            new JsonMatcher(),
            new StringMatcher(),
            new ListMatcher());

    public static MatchResult process(Object actual, Object expected) {

        if (!(expected instanceof Map<?, ?> config)) {
            return SimpleMatcher.eq(actual, expected);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> operatorConfig = (Map<String, Object>) config;
        String op = String.valueOf(operatorConfig.getOrDefault("op", "eq"));
        Object val = operatorConfig.get("val");

        return MATCHERS.stream()
                .filter(m -> m.support(op))
                .findFirst()
                .map(m -> m.match(actual, val, operatorConfig))
                .orElseGet(() -> SimpleMatcher.eq(actual, val));
    }
}
