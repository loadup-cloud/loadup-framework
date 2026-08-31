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
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Numeric comparison matcher supporting gt, ge, lt, le operations. Handles various numeric types
 * through BigDecimal conversion.
 */
public class NumberMatcher implements OperatorMatcher {
    private static final Set<String> OPS = Set.of("gt", "ge", "lt", "le");

    @Override
    public boolean support(String op) {
        return OPS.contains(op);
    }

    @Override
    public MatchResult match(Object actual, Object val, Map<String, Object> config) {
        BigDecimal act = new BigDecimal(actual.toString());
        BigDecimal exp = new BigDecimal(val.toString());
        int res = act.compareTo(exp);

        String op = String.valueOf(config.get("op"));
        boolean passed =
                switch (op) {
                    case "gt" -> res > 0;
                    case "ge" -> res >= 0;
                    case "lt" -> res < 0;
                    case "le" -> res <= 0;
                    default -> false;
                };
        return passed ? MatchResult.pass() : MatchResult.fail(actual, val, "Numeric match failed");
    }
}
