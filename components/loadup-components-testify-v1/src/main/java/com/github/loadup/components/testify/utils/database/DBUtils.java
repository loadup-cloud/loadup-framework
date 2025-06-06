/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.utils.database;

/*-
 * #%L
 * loadup-components-test
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.testify.component.db.DBDatasProcessor;
import com.github.loadup.components.testify.template.TestifyTestBase;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 *
 */
public class DBUtils {
    /**
     * 数据处理
     */
    public static DBDatasProcessor dbDatasProcessor = TestifyTestBase.dbDatasProcessor;

    /**
     * 查询单条记录
     *
     * @param sql
     * @return
     */
    public static Map<String, Object> queryLine(String sql) {
        List<Map<String, Object>> multiResultMap = queryList(sql);
        if (multiResultMap == null || multiResultMap.isEmpty()) {
            return null;
        }
        return multiResultMap.get(0);
    }

    /**
     * 查询结果集
     *
     * @param sql
     * @return
     */
    public static List<Map<String, Object>> queryList(String sql) {
        String tableName = StringUtils.substringBetween(StringUtils.toRootLowerCase(sql), "from", "where")
                .trim();
        return queryList(tableName, sql);
    }

    /**
     * 查询结果集
     *
     * @param sql
     * @return
     */
    public static List<Map<String, Object>> queryList(String tableName, String sql) {
        List<Map<String, Object>> tableList = dbDatasProcessor.queryForList(tableName, sql);
        return tableList;
    }

    /**
     * 执行 insert 或 update 操作
     *
     * @param sql
     * @return
     */
    public static int update(String sql) {
        String tableName = StringUtils.substringBetween(StringUtils.toRootLowerCase(sql), "update", "set")
                .trim();
        // 执行SQL
        return update(tableName, sql);
    }

    /**
     * 执行 insert 或 update 操作
     *
     * @param sql
     * @return
     */
    public static int update(String tableName, String sql) {
        // 执行SQL
        return dbDatasProcessor.getJdbcTemplate(tableName).update(sql);
    }

    /**
     * 获取select语句的表名
     *
     * @param sql
     * @return
     */
    @Deprecated
    public static String getTableName(String sql) {
        // 先转小写
        sql = StringUtils.toRootLowerCase(sql);
        // 解析出sql中的tableName
        String tableName = StringUtils.substringBetween(sql, "from", "where").trim();
        if (StringUtils.isEmpty(tableName)) {
            return null;
        }
        return tableName;
    }
}
