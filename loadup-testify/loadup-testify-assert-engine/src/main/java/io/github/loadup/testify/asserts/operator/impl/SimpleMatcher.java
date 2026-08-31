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
import java.math.BigDecimal;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Simple equality and inequality matcher. Handles null values properly.
 */
public class SimpleMatcher {
    public static MatchResult eq(Object actual, Object expected) {
        if (actual == null && expected == null) {
            return MatchResult.pass();
        }
        if (actual == null || expected == null) {
            return MatchResult.fail(actual, expected, "One of the values is null");
        }

        // 核心：处理数字和字符串的等值比对（如 Long 123 vs Integer 123）
        if (StringUtils.isNumeric(actual.toString()) && StringUtils.isNumeric(expected.toString())) {
            return new BigDecimal(actual.toString()).compareTo(new BigDecimal(expected.toString())) == 0
                    ? MatchResult.pass()
                    : MatchResult.fail(actual, expected, "Numeric values not equal");
        }

        return Objects.equals(actual, expected)
                ? MatchResult.pass()
                : MatchResult.fail(actual, expected, "Values are not equal");
    }
}
