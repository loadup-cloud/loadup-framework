/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
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

import com.github.loadup.components.testify.util.ReflectUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * insert sql log resolver
 *
 * @author chao.gao
 * 
 * 
 * hongling.xiang Exp $
 */
public class InsertSqlLogParser implements SqlLogParser {

    /**
     * @throws ClassNotFoundException
     * @see SqlLogParser#parse(String,
     * String, String)
     */
    @Override
    public List<Map<String, Object>> parseGenTableDatas(String sql,
                                                        List<String> paramValue, List<String> paramType) {

        List<Map<String, Object>> tableDatas = new ArrayList<Map<String, Object>>();
        Map<String, Object> tableRow = new HashMap<String, Object>();

        // --resolve all fields of table
        // field name
        String tableNameFields = sql.substring(sql.indexOf(" into ") + 6,
                sql.indexOf(" values")).trim();
        String tableFields = tableNameFields.substring(
                        tableNameFields.indexOf("(") + 1, tableNameFields.indexOf(")"))
                .trim();

        // field value
        String tableValues = StringUtils.substring(sql,
                sql.indexOf(" values") + 7).trim();

        // 字段名称、值、类型顺序对应
        int index = 0;
        String[] tableFieldsArray = tableFields.split(",");
        String[] tableValuesArray = tableValues.substring(1,
                tableValues.length() - 1).split(",");
        /*
         * //----------然后对于map需要特殊处理-------------
         *
         * String[] parsedArray = new String[tableFieldsArray.length]; int
         * indexResult = 0; for (int i = 0; i < tableValuesArray.length; i++) {
         *
         * if (StringUtils.startsWith(tableValuesArray[i], " '{")) {
         *
         * String newLine = ""; while
         * (!StringUtils.endsWith(tableValuesArray[i], "}'")) { newLine +=
         * tableValuesArray[i]; i++; }
         *
         * newLine += tableValuesArray[i]; parsedArray[indexResult] = newLine; }
         * else { parsedArray[indexResult] = tableValuesArray[i]; }
         *
         * indexResult++; } tableValuesArray = parsedArray;
         * //----------map特殊处理完成-------------
         */
        Object[] tableValuesObj = new Object[tableValuesArray.length];
        for (int i = 0; i < tableValuesArray.length; i++) {
            String fieldValue = tableValuesArray[i].trim();
            if (StringUtils.equals(fieldValue, "?")
                    || StringUtils.equals(fieldValue, "null")) {
                // 特殊处理字段为空或者为null的情况
                if (StringUtils.isBlank(paramType.get(index))) {
                    tableValuesObj[i] = null;
                } else if (StringUtils.equalsIgnoreCase("",
                        paramValue.get(index))) {
                    tableValuesObj[i] = StringUtils.EMPTY;
                } else if (StringUtils.equalsIgnoreCase("null",
                        paramValue.get(index))) {
                    tableValuesObj[i] = null;
                } else {
                    Class<?> clazz = ReflectUtil.getClassForName(paramType.get(
                            index).trim());
                    Object obj = ReflectUtil.valueByCorrectType(null, clazz,
                            paramValue.get(index).trim());
                    tableValuesObj[i] = obj;
                }

                index++;
            }
            // field value is a sql function
            if (StringUtils.equals(fieldValue, "sysdate")) {
                tableValuesObj[i] = new Date();
            }
            tableRow.put(tableFieldsArray[i].trim(), tableValuesObj[i]);
        }

        if (null != tableRow) {
            tableDatas.add(tableRow);
        }

        return tableDatas;
    }

    /**
     * @see SqlLogParser#parseTableName(String)
     */
    @Override
    public String parseTableName(String sql) {
        String tableNameFields = sql.substring(sql.indexOf(" into") + 5,
                sql.indexOf(" values")).trim();
        return tableNameFields.substring(0, tableNameFields.indexOf("("))
                .trim();
    }

    /**
     * @see SqlLogParser#parseTableFlags(String,
     * Set)
     */
    @Override
    public Map<String, String> parseTableFlags(String sql,
                                               Set<String> tableFields) {

        Map<String, String> fieldFlag = new HashMap<String, String>();

        // field named ID is the primary key in default, every field will be
        // checked in default
        for (String field : tableFields) {
            if (StringUtils.equalsIgnoreCase(field, "ID")) {
                fieldFlag.put(field, "C");
                continue;
            }
            fieldFlag.put(field, "Y");
        }

        return fieldFlag;
    }

}
