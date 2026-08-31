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
 * Simple equality and inequality matcher. Handles null values properly.
 */
public class StringMatcher implements OperatorMatcher {
    @Override
    public boolean support(String op) {
        return "contains".equals(op);
    }

    @Override
    public MatchResult match(Object actual, Object val, Map<String, Object> config) {
        String actStr = String.valueOf(actual);
        String expStr = String.valueOf(val);

        boolean matched = actStr.contains(expStr);
        return matched
                ? MatchResult.pass()
                : MatchResult.fail(actual, val, "Actual string does not contain expected substring");
    }
}
