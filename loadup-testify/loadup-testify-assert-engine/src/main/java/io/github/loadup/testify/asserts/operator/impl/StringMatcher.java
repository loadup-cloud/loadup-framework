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
import java.util.Map;

/** Simple equality and inequality matcher. Handles null values properly. */
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
