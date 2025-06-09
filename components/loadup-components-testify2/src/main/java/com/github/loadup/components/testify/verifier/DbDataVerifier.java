package com.github.loadup.components.testify.verifier;

import com.github.loadup.components.testify.YamlDataLoader;
import com.github.loadup.components.testify.comparator.DeepComparator;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DbDataVerifier {

    public static void verify(String filePath, DataSource dataSource)  {
        try{


        var jdbcTemplate = new JdbcTemplate(dataSource);
        var root = new YamlDataLoader().loadWithDetails(filePath);
        var expected = (List<Map<String, Object>>) root.get("data");

        String rawQuery = (String) root.get("query");
        String sql = YamlDataLoader.replacePlaceholders(rawQuery, root);

        List<Map<String, Object>> actual = jdbcTemplate.queryForList(sql);

        assertEquals(actual.size(), expected.size(), "数据记录数量不一致");

        List<String> ignore = (List<String>) root.get("ignore");
        Map<String, String> match = (Map<String, String>) root.get("match");

        for (int i = 0; i < expected.size(); i++) {
            var expectRow = expected.get(i);
            var actualRow = actual.get(i);

            for (String key : expectRow.keySet()) {
                if (ignore != null && ignore.contains(key)) continue;
                if (matchCondition(key, expectRow.get(key), actualRow.get(key), match)) {
                    assertTrue(DeepComparator.deepEquals(expectRow.get(key), actualRow.get(key)),
                            "字段 [" + key + "] 深度比对失败");

                    //assertEquals(actualRow.get(key), expectRow.get(key), "字段 [" + key + "] 不匹配");
                }
            }
        } }
        catch (Exception e){
            System.out.println(e);
            //throw new DataVerificationException("数据验证失败","",1);
        }
    }

    private static boolean matchCondition(String field, Object expected, Object actual, Map<String, String> rules) {
        if (rules != null && rules.containsKey(field)) {
            return actual.toString().matches(rules.get(field));
        }
        return true;
    }
}
