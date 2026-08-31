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
import java.util.Map;

/**
 * Regular expression matcher with pattern caching for performance.
 */
public class RegexMatcher implements OperatorMatcher {
    @Override
    public boolean support(String op) {
        return "regex".equals(op);
    }

    @Override
    public MatchResult match(Object actual, Object val, Map<String, Object> config) {
        if (actual == null) {
            return MatchResult.fail(null, val, "Actual value is null");
        }

        String regex = String.valueOf(val);
        boolean matches = String.valueOf(actual).matches(regex);

        return matches
                ? MatchResult.pass()
                : MatchResult.fail(actual, val, "Actual value does not match regex pattern: " + regex);
    }
}
