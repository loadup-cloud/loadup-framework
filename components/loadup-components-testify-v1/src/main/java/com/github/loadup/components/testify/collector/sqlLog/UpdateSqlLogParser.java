/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.collector.sqlLog;

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

import com.github.loadup.components.testify.runtime.TestifyRuntimeContextThreadHold;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * 更新sql日志解析器
 *
 *
 *
 */
public class UpdateSqlLogParser implements SqlLogParser {

    /**
     * insert sql正则表达式
     */
    private static final Pattern fieldSetParttner = Pattern.compile("([\\s\\S]*)in \\(([\\s\\S]*)\\)");

    /**
     * @see SqlLogParser#parse(String, String, String)
     */
    @Override
    public List<Map<String, Object>> parseGenTableDatas(String sql, List<String> paramValue, List<String> paramType) {

        String querySql = genSql(sql, paramValue, paramType);

        String tableName = parseTableName(sql);
        // 执行sql查询
        return TestifyRuntimeContextThreadHold.getContext()
                .getDbDatasProcessor()
                .queryForList(tableName, querySql);
    }

    /**
     * 生成select的sql
     *
     * @param sql
     * @param paramValue
     * @return
     */
    public String genSql(String sql, List<String> paramValue, List<String> paramType) {
        // 需要解析获取set部分字段及where条件涉及字段
        String setFieldStr = sql.substring(sql.indexOf(" set ") + 5, sql.indexOf(" where "));
        String[] setFields = setFieldStr.trim().split(",");
        if (null == setFields || setFields.length == 0) {
            return null;
        }

        String condtionPart = sql.substring(sql.indexOf(" where ")).trim();
        // 计算set条件需要更新的字段数
        int needUpdateFieldNum = 0;
        for (String field : setFields) {
            if (StringUtils.contains(field, "?")) {
                needUpdateFieldNum++;
            }
        }
        while (condtionPart.contains("?")) {
            // 条件字段值替换
            condtionPart = StringUtils.replace(condtionPart, "?", "'" + paramValue.get(needUpdateFieldNum) + "'", 1);
            ++needUpdateFieldNum;
        }

        String tableName = parseTableName(sql);
        StringBuffer querySql = new StringBuffer("select * from ");
        querySql.append(tableName);
        querySql.append(" " + condtionPart);
        querySql.append("\r\n");
        return querySql.toString();
    }

    /**
     * @see SqlLogParser#parseTableName(String)
     */
    @Override
    public String parseTableName(String sql) {

        String key = "update";
        String sqlPart =
                sql.substring(sql.indexOf(key) + key.length(), sql.length()).trim();
        String[] sqlSegments = sqlPart.split(" ");

        return sqlSegments[0].trim();
    }

    /**
     * @see SqlLogParser#parseTableFlags(String, Set)
     */
    @Override
    public Map<String, String> parseTableFlags(String sql, Set<String> tableFields) {

        Map<String, String> fieldFlag = new HashMap<String, String>();

        // 条件字段
        Set<String> conFieldSet = new HashSet<String>();
        String condtionFieldStr = sql.substring(sql.indexOf("where") + 5).trim();
        List<String> conFields = Arrays.asList(condtionFieldStr.toLowerCase().split(" and "));
        for (int i = 0; i < conFields.size(); i++) {
            // 条件字段值为集合,形如id in (?,?,?)
            if (fieldSetParttner.matcher(conFields.get(i)).find()
                    && conFields.get(i).contains("?")) {
                // 去除括号
                String newConField = StringUtils.replace(conFields.get(i), "(", "");
                newConField = StringUtils.replace(newConField, ")", "");
                conFieldSet.add(StringUtils.substring(newConField, 0, newConField.indexOf("in"))
                        .trim());
                continue;
            }

            if (conFields.get(i).contains("?") && conFields.get(i).contains("=")) {
                // 去除括号
                String newConField = StringUtils.replace(conFields.get(i), "(", "");
                conFields.set(i, StringUtils.replace(newConField, ")", ""));
                conFieldSet.add(StringUtils.substring(newConField, 0, newConField.indexOf("="))
                        .trim());
            }
        }

        // 为字段设置标记
        for (String field : tableFields) {
            if (conFieldSet.contains(field)) {
                fieldFlag.put(field, "C");
                continue;
            }
            fieldFlag.put(field, "Y");
        }

        return fieldFlag;
    }
}
