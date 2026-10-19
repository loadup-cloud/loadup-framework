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
import io.github.loadup.testify.asserts.model.RowDiff;
import io.github.loadup.testify.asserts.operator.OperatorProcessor;
import io.github.loadup.testify.asserts.util.ColumnNormalizer;
import io.github.loadup.testify.data.engine.variable.VariableEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

public class DbAssertEngine implements TestifyAssertEngine {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");

    private final JdbcTemplate jdbcTemplate;
    private final VariableEngine variableEngine;

    private String columnNamingStrategy = "caseInsensitive";

    @Override
    public void compare(JsonNode expectNode, Object actualEx, Map<String, Object> context) {
        if (expectNode == null) {
            return;
        }

        Object resolved = variableEngine.resolveValue(expectNode, context);
        JsonNode resolvedExpect = JsonUtil.valueToTree(resolved);

        if (resolvedExpect.isArray()) {
            resolvedExpect.forEach(tableNode -> processTableAssertion(tableNode));
        } else {
            processTableAssertion(resolvedExpect);
        }
    }

    private void processTableAssertion(JsonNode tableNode) {
        String tableName = identifier(tableNode.get("table").asString());
        String mode = tableNode.has("mode") ? tableNode.get("mode").asString() : "lenient";
        JsonNode rowsNode = tableNode.get("rows");

        List<Map<String, Object>> expectedRows = JsonUtil.convertValue(rowsNode, new TypeReference<>() {});
        int timeout = tableNode.path("timeout").asInt(0);
        int interval = tableNode.path("interval").asInt(500);
        long end = System.currentTimeMillis() + timeout;

        AssertionError lastError = null;
        do {
            try {

                List<Map<String, Object>> actualRows = fetchActualRows(tableName, expectedRows);

                verify(tableName, actualRows, expectedRows, mode);
                return;
            } catch (AssertionError e) {
                lastError = e;
                if (timeout > 0 && System.currentTimeMillis() < end) {
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ex);
                    }
                }
            }
        } while (System.currentTimeMillis() < end);

        throw lastError;
    }

    public void verify(
            String tableName,
            List<Map<String, Object>> actualRows,
            List<Map<String, Object>> expectedRows,
            String mode) {
        List<RowDiff> diffs = new ArrayList<>();

        List<Map<String, Object>> normalizedActuals = actualRows.stream()
                .map(row -> ColumnNormalizer.normalizeMap(row, columnNamingStrategy))
                .collect(Collectors.toList());

        // 1. Strict mode enforces the total row count, unless any expected row uses _count
        boolean hasCountCriteria = expectedRows.stream().anyMatch(row -> row.containsKey("_count"));
        if ("strict".equalsIgnoreCase(mode) && !hasCountCriteria && normalizedActuals.size() != expectedRows.size()) {
            Map<String, FieldDiff> countDiffs = new HashMap<>();
            countDiffs.put(
                    "_count",
                    new FieldDiff(
                            "_count", expectedRows.size(), normalizedActuals.size(), "Strict mode row count mismatch"));
            diffs.add(RowDiff.diff(-1, "Row count mismatch", Map.of("_count", expectedRows.size()), countDiffs));
        }

        for (int i = 0; i < expectedRows.size(); i++) {
            Map<String, Object> expectedRow = expectedRows.get(i);
            Map<String, Object> normalizedExpected = ColumnNormalizer.normalizeMap(expectedRow, columnNamingStrategy);
            Map<String, FieldDiff> fieldDiffs = new HashMap<>();

            if (normalizedExpected.containsKey("_count")) {
                long expectedCount =
                        Long.parseLong(normalizedExpected.get("_count").toString());
                long actualCount = countMatchedRows(normalizedActuals, normalizedExpected);

                if (expectedCount != actualCount) {
                    fieldDiffs.put(
                            "_count",
                            new FieldDiff(
                                    "_count",
                                    expectedCount,
                                    actualCount,
                                    String.format(
                                            "Count mismatch for criteria! Expected: %d, Actual: %d",
                                            expectedCount, actualCount)));
                }

                if (expectedCount == 0 && fieldDiffs.isEmpty()) {
                    continue;
                }
            }

            Map<String, Object> actualRow = findMatchedRow(normalizedActuals, normalizedExpected, i);

            if (actualRow == null && !normalizedExpected.containsKey("_count")) {

                diffs.add(RowDiff.missing(i, "No matching row found in DB", expectedRow));
                continue;
            }

            final Map<String, Object> finalActualRow = actualRow;
            normalizedExpected.forEach((field, expVal) -> {
                if ("_match".equals(field) || "_count".equals(field)) {
                    return;
                }

                if (finalActualRow == null) {

                    fieldDiffs.put(field, new FieldDiff(field, expVal, "ABSENT", "Row not found for field validation"));
                } else {
                    Object actVal = finalActualRow.get(field);
                    MatchResult result = OperatorProcessor.process(actVal, expVal);
                    if (!result.isPassed()) {
                        fieldDiffs.put(field, new FieldDiff(field, expVal, actVal, result.message()));
                    }
                }
            });

            if (!fieldDiffs.isEmpty()) {
                diffs.add(RowDiff.diff(i, "Validation failed", expectedRow, fieldDiffs));
            }
        }

        if (!diffs.isEmpty()) {
            List<FieldDiff> finalDiffs = diffs.stream()
                    .flatMap(rd -> {
                        String prefix = tableName + "[" + rd.getIndex() + "]";
                        if ("MISSING".equals(rd.getType())) {
                            return Stream.of(new FieldDiff(prefix, "PRESENT", "ABSENT", rd.getMessage()));
                        }
                        return rd.getFieldDiffs().entrySet().stream()
                                .map(entry -> new FieldDiff(
                                        prefix + "." + entry.getKey(),
                                        entry.getValue().expected(),
                                        entry.getValue().actual(),
                                        entry.getValue().message()));
                    })
                    .collect(Collectors.toList());

            throw new AssertionError(DiffReportBuilder.build("Database Assertion", finalDiffs));
        }
    }

    private long countMatchedRows(List<Map<String, Object>> actuals, Map<String, Object> expectedRow) {
        if (!expectedRow.containsKey("_match")) {
            return actuals.size();
        }

        Map<String, Object> criteria = (Map<String, Object>) expectedRow.get("_match");
        Map<String, Object> normalizedCriteria = ColumnNormalizer.normalizeMap(criteria, columnNamingStrategy);

        return actuals.stream()
                .filter(row -> normalizedCriteria.entrySet().stream()
                        .allMatch(e -> OperatorProcessor.process(row.get(e.getKey()), e.getValue())
                                .isPassed()))
                .count();
    }

    private Map<String, Object> findMatchedRow(
            List<Map<String, Object>> actuals, Map<String, Object> expected, int index) {
        if (!expected.containsKey("_match")) {
            return index < actuals.size() ? actuals.get(index) : null;
        }

        Map<String, Object> criteria = (Map<String, Object>) expected.get("_match");
        Map<String, Object> normalizedCriteria = ColumnNormalizer.normalizeMap(criteria, columnNamingStrategy);

        return actuals.stream()
                .filter(row -> normalizedCriteria.entrySet().stream()
                        .allMatch(e -> OperatorProcessor.process(row.get(e.getKey()), e.getValue())
                                .isPassed()))
                .findFirst()
                .orElse(null);
    }

    private List<Map<String, Object>> fetchActualRows(String tableName, List<Map<String, Object>> expectedRows) {
        List<Map<String, Object>> results = new ArrayList<>();
        boolean hasMatchCriteria = false;

        for (Map<String, Object> row : expectedRows) {
            if (row.containsKey("_match")) {
                hasMatchCriteria = true;
                Map<String, Object> criteria = (Map<String, Object>) row.get("_match");
                StringBuilder sql =
                        new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE 1=1 ");
                List<Object> params = new ArrayList<>();

                criteria.forEach((k, v) -> {
                    if (!(v instanceof Map)) {
                        sql.append(" AND ").append(identifier(k)).append(" = ?");
                        params.add(v);
                    }
                });
                results.addAll(jdbcTemplate.queryForList(sql.toString(), params.toArray()));
            }
        }

        if (results.isEmpty()
                && (!hasMatchCriteria
                        || expectedRows.stream()
                                .anyMatch(r ->
                                        !r.getOrDefault("_count", 1).toString().equals("0")))) {
            return jdbcTemplate.queryForList("SELECT * FROM " + identifier(tableName) + " LIMIT 100");
        }
        return results;
    }

    private String identifier(String value) {
        if (!IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + value);
        }
        return value;
    }

    public DbAssertEngine(JdbcTemplate jdbcTemplate, VariableEngine variableEngine) {
        this.jdbcTemplate = jdbcTemplate;
        this.variableEngine = variableEngine;
    }
}
