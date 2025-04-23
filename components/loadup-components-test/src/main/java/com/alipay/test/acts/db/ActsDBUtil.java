/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2008 All Rights Reserved.
 */
package com.alipay.test.acts.db;

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

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.constant.ActsPathConstants;
import com.alipay.test.acts.db.model.DBConnection;
import com.alipay.test.acts.log.ActsLogUtil;

/**
 * 数据库操作工具类
 * 
 * 注意，虽然方法允许线下调用，但仅会加载默认配置。
 * 线下使用建议直接调用AbstractDBService.getService来初始化
 * 
 * @author baishuo.lp
 * @version $Id: DBUtils.java, v 0.1 2015年8月16日 上午12:08:10 baishuo.lp Exp $
 */
public class ActsDBUtil {
    protected static final Log                 logger                = LogFactory
                                                                         .getLog(ActsDBUtil.class);

    /** DBConfigKey*/
    protected static Properties                DBConfigKeyProperties = new Properties();

    /** 连接池*/
    protected static Map<String, DBConnection> connMap               = new HashMap<String, DBConnection>();

    /**
     * 基于表名从文件获取DBConfigKey
     * 
     * @param tableName
     * @return
     */
    public static String getDBConfigKey(String tableName, String dbConfigKey) {
        if (StringUtils.isNotBlank(dbConfigKey)) {
            return dbConfigKey.trim();
        } else {
            if (DBConfigKeyProperties.isEmpty())
                loadDBConfigKeyMap();
            dbConfigKey = DBConfigKeyProperties.getProperty(tableName);
            Assert.assertNotNull("数据配置Key不能为空", dbConfigKey);
            return dbConfigKey;
        }
    }

    /**
     * 基于DBConfigKeyPath载入所有数据库对应dbConfigKey
     */
    public static void loadDBConfigKeyMap() {
        try {
            InputStream is = ActsDBUtil.class.getClassLoader().getResourceAsStream(
                ActsPathConstants.DBCONFIG_PATH);
            DBConfigKeyProperties.load(is);
        } catch (Exception e) {
            ActsLogUtil.error(logger, "无法从" + ActsPathConstants.DBCONFIG_PATH
                                      + "加载DBConfigKeyMap.properties", e);
        }
    }

    /**
     * 获取数据库连接
     * 
     * @param dbConfigKey
     * @return
     */
    public static DBConnection initConnection(String dbConfigKey) {
        DBConnection conn = connMap.get(dbConfigKey);
        if (conn == null) {
            conn = new DBConnection(dbConfigKey);
            connMap.put(dbConfigKey, conn);
        }
        return conn;
    }

    /**
     * 获取多行数据
     * 
     * @param sql
     * @param tableName
     * @param dbConfigKey
     * @return
     */
    public static List<Map<String, Object>> getMultiQueryResultMap(String sql, String tableName,
                                                                   String dbConfigKey) {
        DBConnection conn = initConnection(getDBConfigKey(tableName, dbConfigKey));
        return conn.executeQuery(sql);
    }

    /**
     * 获取单行数据
     * 
     * @param sql
     * @param tableName
     * @param dbConfigKey
     * @return
     */
    public static Map<String, Object> getQueryResultMap(String sql, String tableName,
                                                        String dbConfigKey) {
        DBConnection conn = initConnection(getDBConfigKey(tableName, dbConfigKey));
        return conn.executeSingleQuery(sql, false);
    }

    /**
     * 执行SQL，指定数据源配置顶
     * 
     * @param sql
     * @param tableName
     * @param dbConfigKey
     * @return
     */
    public static int getUpdateResultMap(String sql, String tableName, String dbConfigKey) {
        DBConnection conn = initConnection(getDBConfigKey(tableName, dbConfigKey));
        return conn.executeUpdate(sql);
    }

    /**
     * 获取sql返回值首个字符串
     * 
     * @param sql
     * @param tableName
     * @param dbConfigKey
     * @return
     */
    public static String getStringValue(String sql, String tableName, String dbConfigKey) {
        DBConnection conn = initConnection(getDBConfigKey(tableName, dbConfigKey));
        return conn.getStringValue(sql);
    }

    /**
     * 如果要走分库数据源，需要自行虚拟分库分表
     * 
     * @param dbConfigKey
     * @return
     */
    public static Date getCurrentDate(String dbConfigKey) {
        DBConnection conn = initConnection(dbConfigKey);
        return conn.getCurrentDate();
    }

    /**
     * 锁数据库记录，仅针对单行数据
     * 
     * @param sql
     * @param tableName
     * @param dbConfigKey
     * @return
     */
    public static Map<String, Object> lockForUpdate(String sql, String tableName, String dbConfigKey) {
        //这里必须新建链接，以防止其他sql不提交
        DBConnection conn = new DBConnection(dbConfigKey);
        connMap.put(dbConfigKey + "_lock", conn);
        return conn.executeSingleQuery(sql, true);
    }

    /**
     * 锁数据库记录释放
     * 
     * @param dbConfigKey
     * @return
     */
    public static boolean lockForUpdateRollBack(String dbConfigKey, boolean isCommit) {
        DBConnection conn = connMap.get(dbConfigKey + "_lock");
        if (conn == null) {
            ActsLogUtil.error(logger, "锁表未初始化");
            return false;
        }
        boolean result = false;
        try {
            if (isCommit) {
                conn.getConn().commit();
            } else {
                conn.getConn().rollback();
            }
            result = true;
        } catch (SQLException e) {
            ActsLogUtil.error(logger, "释放锁失败, dbConfigKey=" + dbConfigKey, e);
        } finally {
            conn.close();
        }
        return result;
    }

    /**
     * Getter method for property <tt>dBConfigKeyProperties</tt>.
     * 
     * @return property value of DBConfigKeyProperties
     */
    public static Properties getDBConfigKeyProperties() {
        return DBConfigKeyProperties;
    }

    /**
     * Getter method for property <tt>connMap</tt>.
     * 
     * @return property value of connMap
     */
    public static Map<String, DBConnection> getConnMap() {
        return connMap;
    }

}
