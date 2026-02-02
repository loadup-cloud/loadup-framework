package io.github.loadup.testify.asserts.operator;

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
import io.github.loadup.testify.asserts.operator.impl.*;
import java.util.List;
import java.util.Map;

public class OperatorProcessor {

    // 建议通过 Spring 自动注入所有的 Matcher 实现类
    private static final List<OperatorMatcher> MATCHERS = List.of(
            new NumberMatcher(),
            new RegexMatcher(),
            new ApproxTimeMatcher(),
            new JsonMatcher(),
            new StringMatcher() // 包含 contains, ne 等
            );

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
