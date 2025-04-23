package com.alipay.test.acts.api;

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

import java.util.List;
import java.util.Map;

import com.alipay.test.acts.db.ActsDBUtil;
import com.alipay.test.acts.db.SyncDBUtil;
import com.alipay.test.acts.db.offlineService.AbstractDBService;

/**
 * 数据库操作工具类，注意，本类中所有方法均无需启用框架即可执行
 * 
 * @author baishuo.lp
 * @version $Id: DbOperateApis.java, v 0.1 2015年3月7日 上午11:56:09 baishuo.lp Exp $
 */
public class DbOperateApis {

    /**
     * 基于表名获取数据库dbConfigKey，默认为空时，从"config/DBConfigKey.properties"中读取
     * 
     * @param tableName
     * @return
     */
    public static String getDBConfigKeyByTableName(String tableName) {
        return ActsDBUtil.getDBConfigKey(tableName, null);
    }

    /**
     * 执行数据库更新语句
     * dbConfigKey为空时，从"config/DBConfigKey.properties"中读取
     * 
     * @param sql
     * @return
     * @throws Exception
     */
    public static int executeUpdate(String sql, String tableName, String dbConfigKey) {
        return ActsDBUtil.getUpdateResultMap(sql, tableName, dbConfigKey);
    }

    /**
     * 执行数据库Query语句
     * dbConfigKey为空时，从"config/DBConfigKey.properties"中读取
     * 
     * @param sql
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> executeQuery(String sql, String tableName,
                                                         String dbConfigKey) throws Exception {
        return ActsDBUtil.getMultiQueryResultMap(sql, tableName, dbConfigKey);
    }

    /**
     * 执行query数据库单点数值
     * dbConfigKey为空时，从"config/DBConfigKey.properties"中读取
     * 
     * @param sql
     * @return
     * @throws Exception
     */
    public static String querySingleValue(String sql, String tableName, String dbConfigKey)
                                                                                           throws Exception {
        return ActsDBUtil.getStringValue(sql, tableName, dbConfigKey);
    }

    /**
     * 获取线下数据库服务
     * 
     * （如果不想在ats-config.properties中配置，可以用这个）
     * 
     * @param url 数据库链接
     * @param userName 数据库用户名（oracle和mysql需要）
     * @param password 数据库密码（oracle和mysql需要）
     * @param schema （oracle需要）
     * @return 线下数据库服务对象
     */
    public static AbstractDBService getOfflineDBService(String url, String userName,
                                                        String password, String schema) {
        return AbstractDBService.getService(url, userName, password, schema);
    }

    /**
     * 从OB同步缓存表入本地csv文件（若文件已存在则按顺序合并）
     * 
     * @param DBURL
     * @param cacheCSVFolder，test bundle resource目录下相对路径
     * @param tableNames
     */
    public static void syncFromOB(String DBURL, String cacheCSVFolder, List<String> tableNames) {
        SyncDBUtil.syncFromOB(DBURL, cacheCSVFolder, tableNames);
    }

    /**
     * 同步本地所有缓存表入OB
     * 
     * @param DBURL
     * @param cacheCSVFolder，test bundle resource目录下相对路径
     * @throws Exception
     */
    public static void syncAllTablesToOB(String DBURL, String cacheCSVFolder) throws Exception {
        SyncDBUtil.syncAllTablesToOB(DBURL, cacheCSVFolder);
    }

    /**
     * 同步本地缓存表指定行入OB
     * 
     * @param DBURL
     * @param cacheCSVFolder，test bundle resource目录下相对路径
     * @param tableName
     * @throws Exception
     */
    public static void syncSelectedTableToOB(String DBURL, String cacheCSVFolder, String tableName)
                                                                                                   throws Exception {
        SyncDBUtil.syncSelectedTableToOB(DBURL, cacheCSVFolder, tableName);
    }

    /**
     * 同步本地缓存表指定行入OB
     * 
     * @param DBURL
     * @param cacheCSVFolder，test bundle resource目录下相对路径
     * @param tableName
     * @param cols，例如"1,2,3"，基准为1
     * @throws Exception
     */
    public static void syncSelectedTableToOB(String DBURL, String cacheCSVFolder, String tableName,
                                             String cols) throws Exception {
        SyncDBUtil.syncSelectedTableToOB(DBURL, cacheCSVFolder, tableName, cols);
    }
}
