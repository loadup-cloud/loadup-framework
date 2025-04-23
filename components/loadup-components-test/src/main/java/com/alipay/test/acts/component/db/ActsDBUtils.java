/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.db;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.model.VirtualTable;
import com.alipay.test.acts.utils.config.ConfigrationFactory;

/**
 * 
 * @author tianzhu.wtzh
 * @version $Id: ActsDBUtils.java, v 0.1 2015年11月4日 下午10:31:58 tianzhu.wtzh Exp $
 */
public class ActsDBUtils {

    /**
     * 执行update操作,支持指定数据源
     * 
     * @param sql
     * @param tableName
     * @return
     */
    public static int getUpdateResultMap(String sql, String tableName, String dbConfigKey) {

        JdbcTemplate jdbcTemplate = getJdbcTemplate(tableName, dbConfigKey);
        int result = jdbcTemplate.update(sql);
        return result;

    }

    /**
     * 执行update操作,不支持指定数据源
     * 
     * @param sql
     * @param tableName
     * @return
     */
    public static int getUpdateResultMap(String sql, String tableName) {
        DBDatasProcessor processor = new DBDatasProcessor();
        processor.initDataSource();

        return processor.getJdbcTemplate(tableName).update(sql);
    }

    /**
     * 执行query操作，指定数据源
     * 
     * @param sql
     * @param tableName
     * @return
     */
    public static List<Map<String, Object>> getQueryResultMap(String sql, String tableName,
                                                              String dbConfigKey) {

        JdbcTemplate jdbcTemplate = getJdbcTemplate(tableName, dbConfigKey);
        List<Map<String, Object>> map = jdbcTemplate.queryForList(sql);

        return map;
    }

    /**
     * 执行query操作，不指定数据源
     * 
     * @param sql
     * @param tableName
     * @return
     */
    public static List<Map<String, Object>> getQueryResultMap(String sql, String tableName) {

        DBDatasProcessor processor = new DBDatasProcessor();
        processor.initDataSource();

        return processor.getJdbcTemplate(tableName).queryForList(sql);
    }

    /**
     * 获取jdbc连接
     * 
     * @param tableName
     * @param dbConfigKey
     * @return
     */
    public static JdbcTemplate getJdbcTemplate(String tableName, String dbConfigKey) {
        String bundleNameAndBeanName = ConfigrationFactory.getConfigration().getPropertyValue(
            "datasource_bean_name_" + dbConfigKey);

        if (StringUtils.isBlank(bundleNameAndBeanName)) {
            throw new RuntimeException();
        }

        String[] args = bundleNameAndBeanName.split(";");
        String bundleName = args[0];
        String dataSourceBeanName = args[1];
        DBDatasProcessor processor = new DBDatasProcessor();
        DataSource ds = null;

        ds = processor.getDataSource(dataSourceBeanName, bundleName);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        return jdbcTemplate;

    }

    /**
     * clean DB data by model
     * 
     * @param tableName
     * @param groupIds
     */
    public static void cleanDBData(VirtualTable tableName, String... groupIds) {
        List<VirtualTable> depTables = new ArrayList<VirtualTable>();
        depTables.add(tableName);
        DBDatasProcessor processor = new DBDatasProcessor();
        processor.initDataSource();
        processor.cleanDBDatas(depTables, groupIds);
    }

    /**
     * prepare db data by model
     * 
     * @param tableName
     * @param groupIds
     */
    public static void preDBData(VirtualTable tableName, String... groupIds) {
        List<VirtualTable> depTables = new ArrayList<VirtualTable>();
        depTables.add(tableName);
        DBDatasProcessor processor = new DBDatasProcessor();
        processor.initDataSource();
        processor.importDepDBDatas(depTables, groupIds);
    }

    /**
     * compare Db Data by model
     * 
     * @param tableName
     * @param groupIds
     */
    public static void compareDbData(VirtualTable tableName, String... groupIds) {
        List<VirtualTable> depTables = new ArrayList<VirtualTable>();
        depTables.add(tableName);
        DBDatasProcessor processor = new DBDatasProcessor();
        processor.initDataSource();
        processor.compare2DBDatas(depTables, groupIds);
    }

}
