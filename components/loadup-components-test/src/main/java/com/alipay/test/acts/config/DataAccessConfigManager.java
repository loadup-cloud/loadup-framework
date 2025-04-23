package com.alipay.test.acts.config;

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

public abstract class DataAccessConfigManager implements InitializingBean {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(DataAccessConfigManager.class);

    /**
     * jdbc连接信息
     */
    protected static Map<String, DataAccessConfig> configMap = new HashMap<String, DataAccessConfig>();

    public static Map<String, List<String>> dataSourceMap = new HashMap<String, List<String>>();
    public static Map<String, List<String>> extDataSourceMap = new HashMap<String, List<String>>();
    /**
     * dao的集合，需要把涉及的DAO都放进来
     * DO的类名作为key--直接使用开发工程的do类
     * value为对应的DAO类对象--将开发工程里的dal接口和实现及对应的ibatis生成的sql迁到测试工程中
     */
    protected static Map<String, Object> DAOMap = new HashMap<String, Object>();

    public String findTableName(String className) {

        Iterator<String> it = configMap.keySet().iterator();

        while (it.hasNext()) {
            if (StringUtils.equals(className, it.next())) {
                return configMap.get(className).getTableName();
            }
        }

        return null;

    }

    public static String findDataSourceName(String targetTableName) {
        return "dataSource";
//        String result = null;
//        String tableName = targetTableName.toLowerCase().trim();
//        String fullMatch = null;
//        String regExpMatch = null;
//
//        for (String key : extDataSourceMap.keySet()) {
//            //默认返回最后一个数据源
//            result = key;
//            for (String tableTmp : extDataSourceMap.get(key)) {
//                String table = tableTmp.toLowerCase().trim();
//                if (tableName.equals(table)) {
//                    fullMatch = key;
//                }
//                if (table.startsWith("*") && !table.endsWith("*")) {
//                    if (tableName.endsWith(table.replace("*", ""))) {
//                        regExpMatch = key;
//                    }
//
//                } else if (!table.startsWith("*") && table.endsWith("*")) {
//                    if (tableName.startsWith(table.replace("*", ""))) {
//                        regExpMatch = key;
//                    }
//
//                } else if (table.startsWith("*") && table.endsWith("*")) {
//                    if (tableName.contains(table.replace("*", ""))) {
//                        regExpMatch = key;
//                    }
//                }
//            }
//        }
//        for (String key : dataSourceMap.keySet()) {
//            //默认返回最后一个数据源
//            result = key;
//            for (String tableTmp : dataSourceMap.get(key)) {
//                String table = tableTmp.toLowerCase().trim();
//                if (tableName.equals(table)) {
//                    fullMatch = key;
//                }
//                if (table.startsWith("*") && !table.endsWith("*")) {
//                    if (tableName.endsWith(table.replace("*", ""))) {
//                        regExpMatch = key;
//                    }
//
//                } else if (!table.startsWith("*") && table.endsWith("*")) {
//                    if (tableName.startsWith(table.replace("*", ""))) {
//                        regExpMatch = key;
//                    }
//
//                } else if (table.startsWith("*") && table.endsWith("*")) {
//                    if (tableName.contains(table.replace("*", ""))) {
//                        regExpMatch = key;
//                    }
//
//                }
//            }
//        }
//
//        // 优先匹配全名，其次是正则匹配
//        if (fullMatch != null || regExpMatch != null) {
//            return fullMatch != null ? fullMatch : regExpMatch;
//        }
//
//        //增加正则表达式匹配，为了兼容老格式，原先的星号匹配不删除
//        for (String key : extDataSourceMap.keySet()) {
//            //默认返回最后一个数据源
//            result = key;
//            for (String tableTmp : extDataSourceMap.get(key)) {
//                String table = tableTmp.toLowerCase().trim();
//                try {
//                    Pattern pattern = Pattern.compile(table);
//                    Matcher matcher = pattern.matcher(tableName);
//                    //匹配到，且匹配到的子字符串与被查字符串完全相等
//                    if (matcher.find()
//                            && StringUtils.equals(table, matcher.group())) {
//                        return key;
//                    }
//                } catch (Exception e) {
//                    // regex has problem on format, ignore and continue
//                }
//            }
//        }
//
//
//        for (String key : dataSourceMap.keySet()) {
//            //默认返回最后一个数据源
//            result = key;
//            for (String tableTmp : dataSourceMap.get(key)) {
//                String table = tableTmp.toLowerCase().trim();
//                try {
//                    Pattern pattern = Pattern.compile(table);
//                    Matcher matcher = pattern.matcher(tableName);
//                    //匹配到，且匹配到的子字符串与被查字符串完全相等
//                    if (matcher.find()
//                            && StringUtils.equals(table, matcher.group())) {
//                        return key;
//                    }
//                } catch (Exception e) {
//                    // regex has problem on format, ignore and continue
//                }
//            }
//        }
//
//        LOGGER.info("Not found data source for table: " + tableName + ". Use default data source(last data source): "
//                + result);
//
//        return result;
    }

    public DataAccessConfig findDataAccessConfig(String className) {
        Iterator<String> it = configMap.keySet().iterator();

        while (it.hasNext()) {
            if (StringUtils.equals(className, it.next())) {
                return configMap.get(className);
            }
        }

        return null;

    }

    public Object getDAO(String className) {

        Iterator<String> it = DAOMap.keySet().iterator();

        while (it.hasNext()) {
            if (StringUtils.equals(className, it.next())) {
                return DAOMap.get(className);
            }
        }

        return null;
    }

    public static void updateExtDataSourceMap(String dataSourceName, String tableName) {
        if (!extDataSourceMap.containsKey(dataSourceName)) {
            List<String> tables = new ArrayList<String>();
            extDataSourceMap.put(dataSourceName, tables);
        }
        extDataSourceMap.get(dataSourceName).add(tableName);
    }

    public static void clearExtDataSourceMap() {
        extDataSourceMap.clear();
    }

}
