package io.github.loadup.testify.asserts.engine;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.loadup.testify.asserts.diff.DiffReportBuilder;
import io.github.loadup.testify.asserts.model.FieldDiff;
import io.github.loadup.testify.asserts.model.MatchResult;
import io.github.loadup.testify.asserts.model.RowDiff;
import io.github.loadup.testify.asserts.operator.OperatorProcessor;
import io.github.loadup.testify.asserts.util.ColumnNormalizer;
import io.github.loadup.testify.core.util.JsonUtil;
import io.github.loadup.testify.data.engine.variable.VariableEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
@Slf4j
public class DbAssertEngine implements TestifyAssertEngine {

    private final JdbcTemplate jdbcTemplate;
    private final VariableEngine variableEngine;

    private String columnNamingStrategy = "caseInsensitive";

    @Override
    public String supportKey() {
        return "database";
    }

    @Override
    public void compare(JsonNode expectNode, Object actualEx, Map<String, Object> context, List<String> reportList) {
        /** 对外入口：解析变量并触发单表/多表比对 */
        if (expectNode == null) return;

        // 1. 变量解析：一次性解析整个 expect 块（包括 ${nowTime} 等）
        JsonNode resolvedExpect = variableEngine.resolveJsonNode(expectNode, context);

        if (resolvedExpect.isArray()) {
            resolvedExpect.forEach(tableNode -> processTableAssertion(tableNode, context));
        } else {
            processTableAssertion(resolvedExpect, context);
        }
    }

    private void processTableAssertion(JsonNode tableNode, Map<String, Object> context) {
        String tableName = tableNode.get("table").asText();
        String mode = tableNode.has("mode") ? tableNode.get("mode").asText() : "lenient";
        JsonNode rowsNode = tableNode.get("rows");

        // 2. 转换数据结构
        List<Map<String, Object>> expectedRows = JsonUtil.convertValue(rowsNode, new TypeReference<>() {});
        int timeout = tableNode.path("timeout").asInt(0);
        int interval = tableNode.path("interval").asInt(500);
        long end = System.currentTimeMillis() + timeout;

        // 3. 抓取数据：根据解析后的 rowsNode 构造 SQL
        AssertionError lastError = null;
        do {
            try {
                List<Map<String, Object>> actualRows = fetchActualRows(tableName, expectedRows);
                // 4. 执行验证
                verify(tableName, actualRows, expectedRows, mode);
                return; // 如果成功，直接返回
            } catch (AssertionError e) {
                lastError = e;
                if (timeout > 0) {
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException ex) {
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

        // 标准化实际行 Key (snake_case -> camelCase)
        List<Map<String, Object>> normalizedActuals = actualRows.stream()
                .map(row -> ColumnNormalizer.normalizeMap(row, columnNamingStrategy))
                .collect(Collectors.toList());

        // 1. 严格模式下的行数校验
        if ("strict".equalsIgnoreCase(mode) && normalizedActuals.size() != expectedRows.size()) {
            throw new AssertionError(String.format(
                    "表 [%s] 行数不匹配! 期望: %d, 实际: %d", tableName, expectedRows.size(), normalizedActuals.size()));
        }

        // 2. 逐行比对
        for (int i = 0; i < expectedRows.size(); i++) {
            Map<String, Object> expectedRow = expectedRows.get(i);
            Map<String, Object> normalizedExpected = ColumnNormalizer.normalizeMap(expectedRow, columnNamingStrategy);

            // 寻找匹配行
            Map<String, Object> actualRow = findMatchedRow(normalizedActuals, normalizedExpected, i);

            if (actualRow == null) {
                diffs.add(RowDiff.missing(i, "No matching row found in DB", expectedRow));
                continue;
            }

            // 字段级比对
            Map<String, FieldDiff> fieldDiffs = new HashMap<>();
            normalizedExpected.forEach((field, expVal) -> {
                if ("_match".equals(field)) return;

                Object actVal = actualRow.get(field);
                MatchResult result = OperatorProcessor.process(actVal, expVal);

                if (!result.isPassed()) {
                    fieldDiffs.put(field, new FieldDiff(field, expVal, actVal, result.message()));
                }
            });

            if (!fieldDiffs.isEmpty()) {
                diffs.add(RowDiff.diff(i, "Field validation failed", expectedRow, fieldDiffs));
            }
        }

        // 在 DbAssertEngine 中修改报表抛出逻辑
        if (!diffs.isEmpty()) {
            List<FieldDiff> finalDiffs = diffs.stream()
                    .flatMap(rd -> {
                        if ("MISSING".equals(rd.getType())) {
                            // 将行缺失转换为一条特殊的 FieldDiff
                            return Stream.of(new FieldDiff(
                                    "table[" + tableName + "].row[" + rd.getIndex() + "]",
                                    "PRESENT",
                                    "ABSENT",
                                    rd.getMessage()));
                        }
                        // 将行内每个字段差异转换为 FieldDiff，并补全 path
                        return rd.getFieldDiffs().entrySet().stream()
                                .map(entry -> new FieldDiff(
                                        tableName + "[" + rd.getIndex() + "]." + entry.getKey(),
                                        entry.getValue().expected(),
                                        entry.getValue().actual(),
                                        entry.getValue().message()));
                    })
                    .collect(Collectors.toList());

            throw new AssertionError(DiffReportBuilder.build("Database Assertion", finalDiffs));
        }
    }

    private Map<String, Object> findMatchedRow(
            List<Map<String, Object>> actuals, Map<String, Object> expected, int index) {
        if (!expected.containsKey("_match")) {
            return index < actuals.size() ? actuals.get(index) : null;
        }

        Map<String, Object> criteria = (Map<String, Object>) expected.get("_match");
        Map<String, Object> normalizedCriteria = ColumnNormalizer.normalizeMap(criteria, columnNamingStrategy);

        return actuals.stream()
                .filter(row -> normalizedCriteria.entrySet().stream().allMatch(e -> {
                    // 使用 OperatorProcessor 而不是简单的 equals，这样 _match 里也能用算子
                    return OperatorProcessor.process(row.get(e.getKey()), e.getValue())
                            .isPassed();
                }))
                .findFirst()
                .orElse(null);
    }

    private List<Map<String, Object>> fetchActualRows(String tableName, List<Map<String, Object>> expectedRows) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> row : expectedRows) {
            if (row.containsKey("_match")) {
                Map<String, Object> criteria = (Map<String, Object>) row.get("_match");
                StringBuilder sql =
                        new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE 1=1 ");
                List<Object> params = new ArrayList<>();

                criteria.forEach((k, v) -> {
                    // 只有当 v 是简单值时才加入 SQL 条件
                    if (!(v instanceof Map)) {
                        sql.append(" AND ").append(k).append(" = ?");
                        params.add(v);
                    }
                });
                results.addAll(jdbcTemplate.queryForList(sql.toString(), params.toArray()));
            }
        }

        // 兜底：如果没有 _match 或没搜到，且期望行数较少，尝试全表前100条
        if (results.isEmpty() && !expectedRows.isEmpty()) {
            return jdbcTemplate.queryForList("SELECT * FROM " + tableName + " LIMIT 100");
        }
        return results;
    }
}
