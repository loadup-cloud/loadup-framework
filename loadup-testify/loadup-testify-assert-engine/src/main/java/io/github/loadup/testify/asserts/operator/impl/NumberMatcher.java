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
