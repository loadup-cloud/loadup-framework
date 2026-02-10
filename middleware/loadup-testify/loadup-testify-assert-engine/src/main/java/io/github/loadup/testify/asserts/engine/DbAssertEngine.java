package io.github.loadup.testify.asserts.engine;

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
import java.util.*;
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
        if (expectNode == null) return;

        // 1. 变量解析：支持 ${serviceCode} 等变量在整个数据库断言块中的替换
        Object resolved = variableEngine.resolveValue(expectNode, context);
        JsonNode resolvedExpect = JsonUtil.valueToTree(resolved);

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

        List<Map<String, Object>> expectedRows = JsonUtil.convertValue(rowsNode, new TypeReference<>() {});
        int timeout = tableNode.path("timeout").asInt(0);
        int interval = tableNode.path("interval").asInt(500);
        long end = System.currentTimeMillis() + timeout;

        AssertionError lastError = null;
        do {
            try {
                // 2. 抓取数据：根据 _match 条件精准抓取
                List<Map<String, Object>> actualRows = fetchActualRows(tableName, expectedRows);
                // 3. 执行多维度验证（包含 _count 和明细）
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

        // 标准化实际行 Key
        List<Map<String, Object>> normalizedActuals = actualRows.stream()
                .map(row -> ColumnNormalizer.normalizeMap(row, columnNamingStrategy))
                .collect(Collectors.toList());

        // 1. 严格模式下的行数总量校验（仅当不含自定义 _count 时有效，否则以 _count 为准）
        if ("strict".equalsIgnoreCase(mode) && normalizedActuals.size() != expectedRows.size()) {
            // 注意：如果配置了 _count，strict 模式的逻辑可能需要调整，这里保持基础总量对比
            log.warn(
                    "Strict mode: Table [{}] total row count mismatch. Expected: {}, Actual: {}",
                    tableName,
                    expectedRows.size(),
                    normalizedActuals.size());
        }

        // 2. 逐行比对逻辑
        for (int i = 0; i < expectedRows.size(); i++) {
            Map<String, Object> expectedRow = expectedRows.get(i);
            Map<String, Object> normalizedExpected = ColumnNormalizer.normalizeMap(expectedRow, columnNamingStrategy);
            Map<String, FieldDiff> fieldDiffs = new HashMap<>();

            // --- A. 处理 _count 校验逻辑 ---
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
                // 如果只需校验 count 且 count 为 0，则无需寻找 actualRow 进行明细校验
                if (expectedCount == 0 && fieldDiffs.isEmpty()) {
                    continue;
                }
            }

            // --- B. 寻找匹配行进行明细校验 ---
            Map<String, Object> actualRow = findMatchedRow(normalizedActuals, normalizedExpected, i);

            if (actualRow == null && !normalizedExpected.containsKey("_count")) {
                // 如果既没有匹配到行，也没有声明 _count (即默认为期望存在 1 条)，报错
                diffs.add(RowDiff.missing(i, "No matching row found in DB", expectedRow));
                continue;
            }

            // --- C. 字段级比对 (排除控制符) ---
            final Map<String, Object> finalActualRow = actualRow;
            normalizedExpected.forEach((field, expVal) -> {
                if ("_match".equals(field) || "_count".equals(field)) return;

                if (finalActualRow == null) {
                    // 如果定义了除 _count 外的字段，但没找到行
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

        // 3. 错误报表构建
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
        if (!expectedRow.containsKey("_match")) return actuals.size();

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
                    // 只处理基础类型条件的拼装，复杂算子交由内存过滤
                    if (!(v instanceof Map)) {
                        sql.append(" AND ").append(k).append(" = ?");
                        params.add(v);
                    }
                });
                results.addAll(jdbcTemplate.queryForList(sql.toString(), params.toArray()));
            }
        }

        // 如果没有 _match 或 没查到数据且期望的是非 0 count，尝试全表前100条作为候选集
        if (results.isEmpty()
                && (!hasMatchCriteria
                        || expectedRows.stream()
                                .anyMatch(r ->
                                        !r.getOrDefault("_count", 1).toString().equals("0")))) {
            return jdbcTemplate.queryForList("SELECT * FROM " + tableName + " LIMIT 100");
        }
        return results;
    }
}
