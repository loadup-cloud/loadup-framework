/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.alipay.test.acts.utils;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alipay.acts.helper.generator.db.DbVersionModel;
import com.alipay.test.acts.util.BaseDataUtil;
import com.alipay.test.acts.utils.model.ConnectionModel;
import com.alipay.yaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.sql.*;
import java.util.*;

/**
 * 
 * @author tianzhu.wtzh
 * @version $Id: MemDbUtil.java, v 0.1 2016年5月7日 下午1:25:08 tianzhu.wtzh Exp $
 */
public class MemDbUtil {

    protected static Logger    logger     = LoggerFactory.getLogger(MemDbUtil.class);

    public final static String  shcemaFile                  = "config/dbConf/mem/db-version.yaml";

    private static final String MYSQL_URL_PREFIX        = "jdbc:mysql://";

    private static final String ORACLE_URL_PREFIX       = "jdbc:oracle:";

    private static final String OB_CREATE_TABLE_FLAG    = "table_definition";

    private static final String MYSQL_CREATE_TABLE_FLAG = "Create Table";

    private static final String MYSQL_PORT              = "3306";

    private static final String MYSQL_PORT1             = "3307";

    private static final String ORACLE_CREATE_SPEC_DATATYPE = "TIMESTAMP(6),DATE";

    /**
     * 内存DB表结构维护
     * @return 
     * @throws Exception 
     */
    public static void memdbSchemmaCheck(ClassLoader loader) throws Exception {

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

        URL shcemaUrl = currentClassLoader.getResource(shcemaFile);

        if (shcemaUrl == null) {
            logger.error("can not find mem schema file [" + shcemaFile + "]!");
            Exception e = new Exception(
                "can not find mem mem schema file ["
                        + shcemaFile
                        + "]!"
                        + "Please check the file src/test/resources/config/dbConf/mem/db-version.yaml exists");
            throw e;
        }
        List<String> difTtableName = new ArrayList<String>();
        Yaml yaml = new Yaml(new BaseDataUtil.SelectiveConstructor(loader),
            new BaseDataUtil.MyRepresenter());
        try {
            for (Object data : yaml.loadAll(new FileInputStream(new File(shcemaUrl.getPath())))) {
                String difTtable = compareDbInfo((DbVersionModel) data);
                if (difTtable != null) {
                    difTtableName.add(difTtable);
                }
            }
        } catch (RuntimeException e) {
            Exception exp = new Exception("memdb schema check fail,the error msg:" + e.getMessage());
            throw exp;
        }
        if (!difTtableName.isEmpty()) {
            Exception e = new Exception("当前维护表结构与物理数据库存在不一致，请重新生成建表语句，不一致的表："
                                        + llistToString(difTtableName));
            throw e;
        }
    }

    /**
     * 比较当前维护的建表语句与物理库的版本
     * 
     * @param data
     * @return
     */
    public static String compareDbInfo(DbVersionModel data) {

        ConnectionModel conModel = new ConnectionModel(data.getUrl(), data.getUserName(),
            data.getPassWord());
        Connection con = getConnection(conModel);
        conModel.setConnection(con);
        String createSql = getCreatTableString(conModel, data.getTableName());
        String nowTableMD5 = MD5Util.MD5(createSql);
        if (!nowTableMD5.equals(data.getDbInfoMD5())) {
            return data.getTableName();
        }

        return null;
    }

    /**
     * 根据ConnectionModel 获取Connection
     * 
     * @param connectionModel
     * @return
     */
    public static Connection getConnection(ConnectionModel connectionModel) {
        Driver driver = connectionModel.getDriver();
        if (driver == null) {
            return null;
        }

        Properties props = new Properties();
        props.put("user", connectionModel.getUsername());
        props.put("password", connectionModel.getPassword());
        props.put("remarksReporting", "true");
        Connection con = null;
        try {
            con = driver.connect(connectionModel.getUrl(), props);
        } catch (SQLException e) {
            logger.error("执行sql出现异常", e);
        }

        return con;
    }

    public static String getCreatTableString(ConnectionModel connectionModel, String selectTable) {
        // 获取建表信息
        Connection con = connectionModel.getConnection();
        String createSql = "";
        if (connectionModel.getUrl().startsWith(MYSQL_URL_PREFIX)) {
            String sql = "show create table " + selectTable;

            try {
                if (isOB(connectionModel)) {
                    createSql = (String) executeQuerySql(sql, con).get(0).get(OB_CREATE_TABLE_FLAG);
                } else {
                    createSql = (String) executeQuerySql(sql, con).get(0).get(
                        MYSQL_CREATE_TABLE_FLAG);
                }
                // 得到的建表信息包含存储信息，存在不兼容，这个地方截掉
                int leng = createSql.lastIndexOf(")");
                String result = createSql.substring(0, leng + 1);

                createSql = result + ";\n";

            } catch (Exception e) {

            }
        } else if (connectionModel.getUrl().startsWith(ORACLE_URL_PREFIX)) {

            String result = getOracleCreateTable(getOracleColumn_info(con, selectTable),
                selectTable);
            String indexSql = createOracleIndex(
                getOracleIndex_info(connectionModel.getConnection(), selectTable), selectTable);
            createSql = result + ";" + indexSql;
        }

        return createSql;
    }

    /**
     * SQL执行方法
     * 
     * @param sql
     * @param conn
     * @return
     * @throws Exception
     */
    private static List<Map<String, Object>> executeQuerySql(String sql, Connection conn)
                                                                                         throws Exception {
        Statement stmt;

        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            logger.error("执行sql出现异常:" + sql, e);
        }
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
    }

    /**
     * 是否OB
     * 
     * @param connectionModel
     * @return
     */
    public static boolean isOB(ConnectionModel connectionModel) {

        if (connectionModel.getUrl().contains(MYSQL_URL_PREFIX)
            && !(connectionModel.getUrl().contains(MYSQL_PORT) || connectionModel.getUrl()
                .contains(MYSQL_PORT1))) {
            return true;
        }
        return false;

    }

    /**
     * 获取oracle数据库字段信息
     * 
     * @param con
     * @param table
     * @return
     */
    public static Map<String, Map<String, String>> getOracleColumn_info(Connection con, String table) {
        String sql = "select COLUMN_NAME,DATA_TYPE,DATA_LENGTH,NULLABLE　from all_tab_columns where table_name =UPPER('"
                     + table + "')";
        Statement st = null;
        Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Map<String, String> cloumMap = new LinkedHashMap<String, String>();
                String key = rs.getString("COLUMN_NAME"); // 获取字段名
                cloumMap.put("DATA_TYPE", rs.getString("DATA_TYPE")); // 获取数据类型
                cloumMap.put("DATA_LENGTH", rs.getString("DATA_LENGTH")); // 获取数据长度
                cloumMap.put("NULLABLE", rs.getString("NULLABLE")); // 获取是否为空
                map.put(key, cloumMap);
            }
        } catch (Exception e) {
            logger.error("执行sql获取oracle表信息出现异常" + sql, e);
        }
        return map;

    }

    /**
     * Oracl DB 建表
     * 
     * @param map
     * @param table
     * @return
     */
    public static String getOracleCreateTable(Map<String, Map<String, String>> map, String table) {

        String sql = "create table " + table + "\n";
        String cloumSql = getOracleCloumByMap(map);
        StringBuffer sb = new StringBuffer();
        sb.append(sql).append("(").append(cloumSql).append(")");
        return sb.toString();

    }

    /**
     * 创建oracleDB建表语句
     * 
     * @param map
     * @return
     */
    public static String getOracleCloumByMap(Map<String, Map<String, String>> map) {

        String isNull = "";
        String seprator = "  ";
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            String cloum = entry.getKey();
            String dataType = entry.getValue().get("DATA_TYPE");
            String dataLength = entry.getValue().get("DATA_LENGTH");
            String nullAble = entry.getValue().get("NULLABLE");
            if (nullAble.equals("N")) {
                isNull = "not null";
            }
            if (!(ORACLE_CREATE_SPEC_DATATYPE).contains(dataType)) {
                sb.append(cloum).append(seprator).append(dataType).append("(" + dataLength + ")")
                    .append(seprator).append(isNull).append(",\n");
            } else {
                sb.append(cloum).append(seprator).append(dataType).append(seprator).append(isNull)
                    .append(",\n");
            }
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    /**
     * 获取oracle 索引
     * 
     * @param con
     * @param table
     * @return
     */
    public static Map<String, List<String>> getOracleIndex_info(Connection con, String table) {

        String indexSql = "select table_name, index_name, column_name, column_position from user_ind_columns where  table_name='"
                          + table + "'";

        Statement st = null;
        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        try {
            st = con.createStatement();
            ResultSet rsIndex = st.executeQuery(indexSql);
            while (rsIndex.next()) {
                map = getIndexFromOracle(rsIndex, map);
            }
        } catch (Exception e) {
            logger.error("获取oracle索引出现异常" + indexSql, e);
        }

        return map;
    }

    /**
     * 获取oracle 数据库索引
     * 
     * @param rs
     * @return
     */
    public static Map<String, List<String>> getIndexFromOracle(ResultSet rs,
                                                               Map<String, List<String>> map) {

        try {
            String indexName = rs.getString("index_name");
            if (map.get(indexName) != null) {
                map.get(indexName).add(rs.getString("column_name"));
            } else {
                List<String> list = new ArrayList<String>();
                list.add(rs.getString("column_name"));
                map.put(indexName, list);
            }
        } catch (Exception e) {
            logger.error("获取oracle索引出现异常", e);
        }

        return map;
    }

    /**
     * 索引转化为创建sql
     * 
     * @param map
     * @param tableName
     * @return
     */
    public static String createOracleIndex(Map<String, List<String>> map, String tableName) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String indexCloum = llistToString(entry.getValue());
            if (entry.getKey().contains("PK")) {
                sb.append("alter table ").append(tableName).append(" add constraint ")
                    .append(entry.getKey()).append(" primary key ").append("(" + indexCloum + ")")
                    .append(";\n");
            } else if (entry.getKey().contains("UK")) {
                sb.append("alter table ").append(tableName).append(" add constraint ")
                    .append(entry.getKey()).append(" unique ").append("(" + indexCloum + ")")
                    .append(";\n");
            } else {
                sb.append("create index ").append(entry.getKey()).append(" on ").append(tableName)
                    .append(" (").append(indexCloum).append(");\n");
            }
        }

        return sb.toString();
    }

    /**
     * list to String
     * 
     * @param list
     * @return
     */
    public static String llistToString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));

        return sb.toString();
    }

}
