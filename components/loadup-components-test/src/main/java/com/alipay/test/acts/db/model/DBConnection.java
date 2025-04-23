package com.alipay.test.acts.db.model;

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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.NodeList;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.db.enums.DataBaseTypeEnum;
import com.alipay.test.acts.db.offlineService.AbstractDBService;
import com.alipay.test.acts.db.offlineService.OceanBaseService;
import com.alipay.test.acts.driver.AtsConfiguration;
import com.alipay.test.acts.driver.constants.AtsConstants;
import com.alipay.test.acts.log.ActsLogUtil;
import com.alipay.test.acts.util.BeanUtil;
import com.alipay.test.acts.util.FileUtil;
import com.alipay.test.acts.util.XMLUtil;

/**
 * 数据库链接对象
 * 
 * @author baishuo.lp
 * @version $Id: AbstractDBService.java, v 0.1 2015年8月13日 下午9:38:38 baishuo.lp Exp $
 */
public class DBConnection {

    protected static final Log  logger = LogFactory.getLog(DBConnection.class);

    protected Connection        conn;

    protected String            userName;

    protected String            password;

    protected String            schema;

    protected String            url;

    protected DataBaseTypeEnum  dbType;

    protected boolean           isOnline;

    protected DataSource        dataSource;

    protected AbstractDBService service;

    /**
     * 框架初始化链接构造方法
     * 
     * @param tableName
     * @param dbConfigKey
     */
    public DBConnection(String dbConfigKey) {
        Assert.assertNotNull("数据库配置Key不能为空", dbConfigKey);
        if (AtsConfiguration.isOffLine()) {
            //线下环境
            this.isOnline = false;
            if (dbConfigKey.startsWith("ext")) {
                this.url = AtsConfiguration.getAtsDBProperty(dbConfigKey + "_db_url");
                this.userName = AtsConfiguration.getAtsDBProperty(dbConfigKey + "_db_username");
                this.password = AtsConfiguration.getAtsDBProperty(dbConfigKey + "_db_password");
                this.schema = AtsConfiguration.getAtsDBProperty(dbConfigKey + "_db_schema");
                this.service = AbstractDBService.getService(this.url, this.userName, this.password,
                    this.schema);
                this.dbType = service.getDbType();
                this.conn = service.getConn();
            } else {
                //虽输入数据源模式，但因为为线下，故加载默认数据库链接
                //(仅支持OB模式, Oracle库xml中太多，必须手动指定)
                String dbMode = AtsConfiguration.getSofaConfig("dbmode");

                Assert.assertNotNull("sofa配置中dbmode不能为空", dbMode);

                File dbXMLFolder = FileUtil.getTestResourceFile("sofaconfig/db/");
                if (!dbXMLFolder.exists() || !dbXMLFolder.isDirectory()) {
                    Assert.fail("未找到配置OB数据库XML文件夹/sofaconfig/db/");
                }
                for (File xmlFile : dbXMLFolder.listFiles()) {
                    if (xmlFile.getName().contains(dbMode)) {
                        NodeList nodeList = XMLUtil.loadXMLFile(xmlFile);
                        this.url = XMLUtil.getAttribute(nodeList, "configURL");
                        this.dbType = DataBaseTypeEnum.OCEANBASE;
                        break;
                    }
                }
                this.service = new OceanBaseService(this.url);
                this.conn = service.getConn();
            }
            ActsLogUtil.info(logger, "使用线下数据库链接" + url);
        } else if (StringUtils.equalsIgnoreCase(dbConfigKey.substring(0, 2), "ds")) {
            //线上环境
            this.isOnline = true;
            String ds = dbConfigKey.trim().toLowerCase();
            String dataSourceBeanName = null;
            if (!ds.equals(AtsConstants.DATASOURCE_KEY)
                && ds.startsWith(AtsConstants.DATASOURCE_KEY)) {
                String suffix = ds.replaceFirst(AtsConstants.DATASOURCE_KEY, "");
                String dataSourceBeanKey = AtsConstants.DATA_SOURCE_BEAN_NAME + "_" + suffix;
                dataSourceBeanName = AtsConfiguration.getAtsProperty(dataSourceBeanKey);
            } else {
                dataSourceBeanName = AtsConfiguration
                    .getAtsProperty(AtsConstants.DATA_SOURCE_BEAN_NAME);
            }
            Assert.assertNotNull("数据源名称不能为空", dataSourceBeanName);
            String[] parts = dataSourceBeanName.split(";");
            String bundleName = "";
            if (2 == parts.length) {
                bundleName = parts[0];
                dataSourceBeanName = parts[1];
            }
            String xmlFile = AtsConfiguration.getAtsProperty(AtsConstants.DATA_SOURCE_XML_FILE);
            if (StringUtils.isNotBlank(xmlFile)) {
                // 构建 Spring 上下文
                ApplicationContext ctx = new ClassPathXmlApplicationContext(xmlFile);
                dataSource = (DataSource) ctx.getBean(dataSourceBeanName);
            } else {
                // 从SOFA运行测试上下文中获取Bean
                dataSource = (DataSource) BeanUtil.getBean(dataSourceBeanName, bundleName);
                // 再到虚拟bundle里取一次
                if (dataSource == null) {
                    // 如果没有数据源则以on-the-fly-bundle为准
//                    dataSource = (DataSource) BeanUtil.getBean(dataSourceBeanName,
//                        SofaTestConstants.ON_THE_FLY_BUNDLE_NAME);
                }
            }
//            Assert.assertNotNull("数据源bean不能为空", dataSource);
            ActsLogUtil.info(logger, "使用" + dbConfigKey + "对应线上数据源链接: " + dataSourceBeanName);
            extractDBTypeFromDataSource();
            try {
                this.conn = dataSource.getConnection();
            } catch (SQLException e) {
                ActsLogUtil.fail(logger, "数据源获取链接失败", e);
            }
        } else {
            Assert.fail("数据库dbConfigKey格式有误，无法初始化数据");
        }
    }

    /**
     *  线下手动初始化链接构造方法
     * @param url
     * @param userName
     * @param password
     * @param schema
     * @param isOnline
     */
    public DBConnection(String url, String userName, String password, String schema) {
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.schema = schema;
        this.isOnline = false;
        AbstractDBService service = AbstractDBService.getService(this.url, this.userName,
            this.password, this.schema);
        this.conn = service.getConn();
        this.dbType = service.getDbType();
    }

    /**
     * 重新连接
     */
    public void reConnection() {
        if (isConnClosed()) {
            if (this.dataSource != null) {
                try {
                    this.conn = dataSource.getConnection();
                } catch (SQLException e) {
                    ActsLogUtil.fail(logger, "数据源获取链接失败", e);
                }
            } else if (this.service != null) {
                this.service.initConnection();
                this.conn = this.service.getConn();
            }
        }
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
     * 执行query sql
     * 
     * @param sql
     * @return
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        if (isConnClosed()) {
            reConnection();
        }
        ActsLogUtil.info(logger, "准备执行sql: " + sql);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        try {
            Statement stmt = this.conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    map.put(rsmd.getColumnName(i), resultSet.getObject(i));
                }
                result.add(map);
            }
        } catch (Exception e) {
            ActsLogUtil.error(logger, "数据库Sql执行出错:" + sql, e);
        }
        return result;
    }

    /**
     * 获取sql结果单条数据，可以选择是否锁表
     * 
     * @param sql
     * @param isLock
     * @return
     */
    public Map<String, Object> executeSingleQuery(String sql, boolean isLock) {
        if (isConnClosed()) {
            reConnection();
        }
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (isLock)
                this.conn.setAutoCommit(false);
            PreparedStatement stat = conn.prepareStatement(sql);
            ResultSet resultSet = stat.executeQuery();
            ResultSetMetaData rsmd = resultSet.getMetaData();
            if (resultSet.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    map.put(rsmd.getColumnName(i), resultSet.getObject(i));
                }
            }
        } catch (SQLException e) {
            ActsLogUtil.error(logger, "数据库Sql执行出错:" + sql, e);
        }
        return map;
    }

    /**
     * 执行更新sql语句
     * 
     * @param sql
     *            执行的SQL语句
     * @return 返回executeUpdate结果
     */
    public int executeUpdate(String sql) {
        if (isConnClosed()) {
            reConnection();
        }
        ActsLogUtil.info(logger, "准备执行sql: " + sql);
        try {
            Statement stmt = this.conn.createStatement();
            return stmt.executeUpdate(sql);
        } catch (Exception e) {
            ActsLogUtil.error(logger, "数据库Sql执行出错:" + sql, e);
            return -1;
        }
    }

    /**
     * 通过SQL获取返回值中的首个字符串，指定数据源配置项
     * 
     * @param sql
     */
    public String getStringValue(String sql) {
        if (isConnClosed()) {
            reConnection();
        }
        ActsLogUtil.info(logger, "准备执行sql: " + sql);

        try {
            Statement stmt = this.conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            if (resultSet.next())
                return resultSet.getString(1);
        } catch (SQLException e) {
            ActsLogUtil.error(logger, "数据库Sql执行出错:" + sql, e);
        }
        return null;
    }

    /**
     * 获取数据库时间
     * 
     * @return
     */
    public Date getCurrentDate() {
        if (isConnClosed()) {
            reConnection();
        }
        String sql = null;
        switch (this.dbType) {
            case OCEANBASE:
            case MYSQL:
                sql = "select now()";
                break;
            case ORACLE:
                sql = "select sysdate";
                break;
            default:
                ActsLogUtil.fail(logger, "不支持的数据库类型");
        }
        ActsLogUtil.info(logger, "准备执行sql: " + sql);
        try {
            Statement stmt = this.conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            if (resultSet.next())
                return resultSet.getDate(0);
        } catch (SQLException e) {
            ActsLogUtil.error(logger, "数据库Sql执行出错:" + sql, e);
        }
        return null;
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            ActsLogUtil.error(logger, "关闭数据库连接出错。", e);
        }
    }

    /**
     * 基于数据源信息获取数据库类型
     * 
     * 注意，由于未引入zdal jar包，故未来可能存在兼容性隐患
     */
    private void extractDBTypeFromDataSource() {
        String rawString = ToStringBuilder.reflectionToString(dataSource);
//        Assert
//            .assertTrue("数据源信息包含dataSourceFactoryMap", rawString.contains("dataSourceFactoryMap"));
        String tmp = rawString.substring(rawString.indexOf("dataSourceFactoryMap") + 22);
        String dbTypeCode = tmp.substring(0, tmp.indexOf("="));
        Assert.assertNotNull("数据库类型不能为空", dbTypeCode);
        if (dbTypeCode.equals("OB")) {
            this.dbType = DataBaseTypeEnum.OCEANBASE;
        } else if (dbTypeCode.equals("ORACLE")) {
            this.dbType = DataBaseTypeEnum.ORACLE;
        } else if (dbTypeCode.equals("MYSQL")) {
            this.dbType = DataBaseTypeEnum.MYSQL;
        } else {
            Assert.fail("数据库类型异常");
        }
        ActsLogUtil.info(logger, "线上数据源类型为" + this.dbType);
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

    /**
     * Getter method for property <tt>isOnline</tt>.
     * 
     * @return property value of isOnline
     */
    public boolean isOnline() {
        return isOnline;
    }

}
