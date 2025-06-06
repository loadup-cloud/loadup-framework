package com.github.loadup.components.testify.verifier;

import com.github.loadup.components.testify.YamlDataLoader;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class DbDataVerifier {

    private static final YamlDataLoader loader = new YamlDataLoader(); // 默认使用 YAML

    /**
     * 执行 SQL 文件并校验结果
     *
     * @param filePath   YAML/CSV 文件路径（classpath）
     * @param dataSource 数据源
     */
    public static void verify(String filePath, DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // 加载期望数据
        Map<String, Object> root = loader.loadWithDetails(filePath); // 返回 Map 包含所有字段
        List<Map<String, Object>> expected = (List<Map<String, Object>>) root.get("data");

        // 提取 query 字段并替换变量
        String rawQuery = (String) root.get("query");
        String sql = replacePlaceholders(rawQuery, root);

        // 获取忽略字段与匹配规则
        List<String> ignoreFields = (List<String>) root.get("ignore");
        Map<String, String> matchRules = (Map<String, String>) root.get("match");

        // 执行查询
        List<Map<String, Object>> actual = jdbcTemplate.queryForList(sql);

        // 校验记录数
        assertEquals(actual.size(), expected.size(), "记录数量不一致");

        // 逐条校验字段
        for (int i = 0; i < expected.size(); i++) {
            Map<String, Object> expectedRow = expected.get(i);
            Map<String, Object> actualRow = actual.get(i);

            for (String key : expectedRow.keySet()) {
                if (ignoreFields != null && ignoreFields.contains(key)) continue;

                if (matchCondition(key, expectedRow.get(key), actualRow.get(key), matchRules)) {
                    assertEquals(actualRow.get(key), expectedRow.get(key),
                            "字段 [" + key + "] 不匹配");
                }
            }
        }
    }

    private static String extractQuery(List<Map<String, Object>> data) {
        if (data instanceof List && !data.isEmpty() && data.get(0).containsKey("query")) {
            return (String) ((Map<String, Object>) data.get(0)).get("query");
        }
        throw new IllegalArgumentException("YAML 文件中缺少 'query' 字段");
    }
    private static boolean matchCondition(String field, Object expected, Object actual, Map<String, String> matchRules) {
        if (matchRules != null && matchRules.containsKey(field)) {
            String regex = matchRules.get(field);
            return actual != null && actual.toString().matches(regex);
        }
        return true; // 默认不干预
    }
    private static String replacePlaceholders(String query, Map<String, Object> context) {
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (entry.getValue() instanceof String || entry.getValue() instanceof Number) {
                query = query.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue().toString());
            }
        }
        return query;
    }


}
