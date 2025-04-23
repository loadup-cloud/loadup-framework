package com.alipay.test.acts.db.offlineService;

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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alipay.test.acts.db.enums.DataBaseTypeEnum;
import com.alipay.test.acts.log.ActsLogUtil;
import org.testng.Assert;

/**
 * 线下DB抽象基类，不需要启动框架
 * 
 * @author baishuo.lp
 * @version $Id: AbstractDBService.java, v 0.1 2015年8月13日 下午9:38:38 baishuo.lp Exp $
 */
public abstract class AbstractDBService {

    protected static final Log logger = LogFactory.getLog(AbstractDBService.class);

    protected Connection       conn;

    protected String           userName;

    protected String           password;

    protected String           url;

    protected String           schema;

    protected DataBaseTypeEnum dbType;

    public static AbstractDBService getService(String url, String userName, String password,
                                               String schema) {
        Assert.assertNotNull("数据库链接不能为空", url);
        DataBaseTypeEnum dbType = null;
        if (url.contains("dataId") && url.startsWith("http")) {
            dbType = DataBaseTypeEnum.OCEANBASE;
        } else if (url.contains("oracle"))
            dbType = DataBaseTypeEnum.ORACLE;
        else if (url.contains("mysql"))
            dbType = DataBaseTypeEnum.MYSQL;
        else {
            Assert.fail("不支持的数据库类型");
        }

        switch (dbType) {
            case OCEANBASE:
                return new OceanBaseService(url);
            case ORACLE:
                return new OracleService(url, userName, password, schema);
            case MYSQL:
                return new MySQLService(url, userName, password, schema);
        }
        return null;
    }

    /**
     * 链接是否失效
     * 
     * @return
     */
    public boolean isConnClosed() {
        try {
            return conn.isClosed();
        } catch (Exception e) {
            logger.error("数据库链接未知异常", e);
            return true;
        }
    }

    /**
     * 初始化数据库连接
     * 
     * @return
     * @throws Exception 
     */
    public abstract void initConnection();

    /**
     * 获取所有表信息。
     * 
     * @return
     */
    public List<String> getTableNames() {
        String sql = "show tables";

        List<Map<String, Object>> tableDatas = executeQuerySql(sql);
        List<String> tables = new ArrayList<String>();
        for (Map<String, Object> m : tableDatas) {
            Map.Entry<String, Object> e = m.entrySet().iterator().next();
            if (e != null) {
                tables.add((String) e.getValue());
            }
        }
        return tables;
    }

    /**
     * 获取当前表所有信息
     * 
     * @param tableName
     * @return
     */
    public List<Map<String, Object>> getTableInfo(String tableName) {
        String sql = "desc " + tableName;
        return executeQuerySql(sql);
    }

    /**
     * 获取数据查询结果
     * 
     * @param sql
     * @return 数据结果
     */
    public List<Map<String, Object>> executeQuerySql(String sql) {
        if (isConnClosed())
            initConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowMap = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(columnName);
                    rowMap.put(columnName, value);
                }
                result.add(rowMap);
            }
            return result;
        } catch (Exception e) {
            ActsLogUtil.error(logger, "执行sql异常，sql=" + sql, e);
        }
        return new ArrayList<Map<String, Object>>();
    }

    /**
     * 执行update语句
     * 
     * @param sql
     * @return 影响行数
     */
    public int executeUpdateSql(String sql) {
        if (isConnClosed())
            initConnection();
        try {
            Statement stmt = this.conn.createStatement();
            return stmt.executeUpdate(sql);
        } catch (Exception e) {
            ActsLogUtil.error(logger, "执行sql异常，sql=" + sql, e);
        }
        return -1;
    }

    /**
     * Getter method for property <tt>conn</tt>.
     * 
     * @return property value of conn
     */
    public Connection getConn() {
        return conn;
    }

    /**
     * Getter method for property <tt>userName</tt>.
     * 
     * @return property value of userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Getter method for property <tt>password</tt>.
     * 
     * @return property value of password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Getter method for property <tt>url</tt>.
     * 
     * @return property value of url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Getter method for property <tt>schema</tt>.
     * 
     * @return property value of schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Getter method for property <tt>dbType</tt>.
     * 
     * @return property value of dbType
     */
    public DataBaseTypeEnum getDbType() {
        return dbType;
    }

}
