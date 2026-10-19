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
package io.github.loadup.testify.asserts.engine;

import io.github.loadup.commons.json.JsonUtil;
import io.github.loadup.testify.asserts.diff.DiffReportBuilder;
import io.github.loadup.testify.asserts.model.FieldDiff;
import io.github.loadup.testify.asserts.model.MatchResult;
import io.github.loadup.testify.asserts.operator.OperatorProcessor;
import io.github.loadup.testify.data.engine.variable.VariableEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

public class ExceptionAssertEngine implements TestifyAssertEngine {
    private final VariableEngine variableEngine;

    public ExceptionAssertEngine(VariableEngine variableEngine) {
        this.variableEngine = variableEngine;
    }

    @Override
    public void compare(JsonNode expectEx, Object actual, Map<String, Object> context) {
        if (expectEx == null) {
            return;
        }
        List<FieldDiff> diffs = new ArrayList<>();
        if (actual == null) {
            String expectType = expectEx.get("type").asString();
            diffs.add(new FieldDiff("exception", expectType, "Null", "Expected exception but none"));
            throw new AssertionError(DiffReportBuilder.build("Exception Assertion", diffs));
        }
        if (!(actual instanceof Throwable actualEx)) {
            return;
        }

        Throwable rootCause = getRootCause(actualEx);

        if (expectEx.has("type")) {
            String expectType = expectEx.get("type").asString();
            if (!isTypeMatch(expectType, rootCause)) {
                diffs.add(new FieldDiff(
                        "exception.type", expectType, rootCause.getClass().getSimpleName(), "Exception type mismatch"));
            }
        }

        if (expectEx.has("message")) {
            JsonNode messageNode = expectEx.get("message");
            String actualMsg = rootCause.getMessage();
            Object resolved = variableEngine.resolveValue(messageNode, context);
            String expectedMsg = JsonUtil.valueToTree(resolved).asString();

            MatchResult result = OperatorProcessor.process(actualMsg, expectedMsg);

            if (!result.isPassed()) {
                diffs.add(new FieldDiff("exception.message", expectedMsg, actualMsg, result.message()));
            }
        }

        if (!diffs.isEmpty()) {
            throw new AssertionError(DiffReportBuilder.build("Exception Assertion", diffs));
        }
    }

    private boolean isTypeMatch(String expectType, Throwable actualEx) {
        String fullClassName = actualEx.getClass().getName();
        String simpleName = actualEx.getClass().getSimpleName();

        return fullClassName.equals(expectType) || simpleName.equalsIgnoreCase(expectType);
    }

    private Throwable getRootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}
