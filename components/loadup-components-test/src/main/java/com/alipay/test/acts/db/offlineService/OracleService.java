/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
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

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.db.enums.DataBaseTypeEnum;
import com.alipay.test.acts.log.ActsLogUtil;

/**
 * Oracle服务
 * 
 * @author baishuo.lp
 * @version $Id: OracleService.java, v 0.1 2015年8月13日 下午9:58:56 baishuo.lp Exp $
 */
public class OracleService extends AbstractDBService {

    static {
        try {
//            Driver driver = new oracle.jdbc.driver.OracleDriver();
//            DriverManager.registerDriver(driver);
        } catch (Exception e) {
            ActsLogUtil.error(logger, "oracle driver注册失败", e);
        }
    }

    public OracleService(String url, String userName, String password, String schema) {
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.schema = schema;
        this.dbType = DataBaseTypeEnum.ORACLE;
        this.initConnection();
    }

    @Override
    public void initConnection() {
        try {
            if (conn == null) {
                conn = DriverManager.getConnection(url, userName, password);
            } else if (this.isConnClosed()) {
                conn = DriverManager.getConnection(url, userName, password);
            }
            if (StringUtils.isNotBlank(this.schema)) {
                String sql = "ALTER SESSION SET CURRENT_SCHEMA=" + this.schema.trim();
                try {
                    Statement stamt = this.conn.createStatement();
                    stamt.execute(sql);
                    stamt.close();
                } catch (SQLException e) {
                    ActsLogUtil.fail(logger, "切换schema报错" + this.schema, e);
                }
            }
        } catch (Exception e) {
            ActsLogUtil.fail(logger, "初始化数据库异常", e);
        }
    }

    /** 
     * @see AbstractDBService#getTableNames()
     */
    @Override
    public List<String> getTableNames() {
        String getTableNameSql = "select distinct table_name from all_synonyms";
        List<Map<String, Object>> tableNameData = executeQuerySql(getTableNameSql);
        if (tableNameData.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> tables = new ArrayList<String>();
        for (Map<String, Object> m : tableNameData) {
            tables.add((String) m.get("TABLE_NAME"));
        }
        return tables;
    }

    @Override
    public List<Map<String, Object>> getTableInfo(String tableName) {
        String tableNameUpper = tableName.toUpperCase();
        String getTableNameSql = "select table_name from all_synonyms where synonym_name='"
                                 + tableNameUpper + "'";
        List<Map<String, Object>> tableNameData = executeQuerySql(getTableNameSql);
        if (tableNameData.isEmpty()) {
            return new ArrayList<Map<String, Object>>();
        }
        tableNameUpper = (String) tableNameData.get(0).get("TABLE_NAME");

        return getTableDesc(tableNameUpper);
    }

    private List<Map<String, Object>> getTableDesc(String tableName) {
        String getDescSql = String
            .format(
                "select COLUMN_NAME, K.DATA_TYPE,K.DATA_LENGTH,K.NULLABLE,K.DATA_DEFAULT,K.DATA_PRECISION,K.DATA_SCALE,C.COMMENTS from all_col_comments C join all_tab_cols K using(TABLE_NAME,COLUMN_NAME) where table_name = UPPER ('%s') order by TABLE_NAME, K.COLUMN_ID",
                tableName);
        String getPrimarySql = String
            .format(
                "SELECT cols.column_name FROM all_constraints cons, all_cons_columns cols WHERE cols.table_name = UPPER ('%s') AND cons.constraint_type = 'P' AND cons.constraint_name = cols.constraint_name ORDER BY cols.table_name, cols.position",
                tableName);
        List<Map<String, Object>> descMap = executeQuerySql(getDescSql);
        List<Map<String, Object>> primaryMap = executeQuerySql(getPrimarySql);
        Set<String> primarySet = new HashSet<String>();
        for (Map<String, Object> dataMap : primaryMap) {
            primarySet.add((String) dataMap.get("COLUMN_NAME"));
        }
        List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> dataMap : descMap) {
            Map<String, Object> realMap = new HashMap<String, Object>();
            String columnName = (String) dataMap.get("COLUMN_NAME");
            realMap.put("field", columnName);
            realMap.put("key", primarySet.contains(columnName) ? 1 : 0);
            String type = (String) dataMap.get("DATA_TYPE");
            if (type.startsWith("TIMESTAMP")) {
                type = "TIMESTAMP";
            } else if (type.startsWith("VARCHAR")) {
                type = "VARCHAR(" + dataMap.get("DATA_LENGTH") + ")";
            } else if (type.equals("NUMBER")) {
                type = "NUMERIC(" + dataMap.get("DATA_PRECISION") + "," + dataMap.get("DATA_SCALE")
                       + ")";
            }
            realMap.put("type", type);
            realMap.put("comment", dataMap.get("COMMENTS"));
            realMap.put("nullable",
                StringUtils.equals((String) dataMap.get("NULLABLE"), "Y") ? "YES" : "NO");
            realMap.put("default", dataMap.get("DATA_DEFAULT"));
            map.add(realMap);
        }
        return map;
    }

}
