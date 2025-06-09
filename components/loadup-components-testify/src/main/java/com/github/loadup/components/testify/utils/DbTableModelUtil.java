/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.utils;

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

import com.github.loadup.components.testify.component.db.DBDatasProcessor;
import com.github.loadup.components.testify.config.DataAccessConfigManager;
import com.github.loadup.components.testify.constant.TestifyPathConstants;
import com.github.loadup.components.testify.db.enums.CSVColEnum;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.util.FileOperateUtils;
import com.github.loadup.components.testify.util.FileUtil;
import com.github.loadup.components.testify.util.FullTableAnalysis;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 数据模版生成工具
 */
@Slf4j
public class DbTableModelUtil {

    /**
     * 查询系统所有的业务表名
     *
     * @param appRoot
     * @return
     */
    public static HashSet<String> queryAppTable(String appRoot) {

        // 1. 获取dal-config.xml;
        List<File> dalConfigFileList = new ArrayList<File>();

        // PathUtil.getAppPath()
        FileOperateUtils.findFileRecursive(appRoot, "dal-config.xml", dalConfigFileList);

        // 2. 解析dal-config.xml获取系统全量表
        HashSet<String> tableSet = new HashSet<String>();

        for (File dalConfigXml : dalConfigFileList) {
            tableSet.addAll(FullTableAnalysis.getFullTableByDalConfig(dalConfigXml));
        }

        return tableSet;
    }

    /**
     * 生成应用的所有表模版
     *
     * @param appRoot
     * @param dbType  支持三种
     */
    public static void generateAppTableModel(String appRoot, String dbType) {

        // 1. 获取dal-config.xml;
        List<File> dalConfigFileList = new ArrayList<File>();

        // PathUtil.getAppPath()
        FileOperateUtils.findFileRecursive(appRoot, "dal-config.xml", dalConfigFileList);

        // 2. 解析dal-config.xml获取系统全量表
        HashSet<String> tableSet = new HashSet<String>();

        for (File dalConfigXml : dalConfigFileList) {
            tableSet.addAll(FullTableAnalysis.getFullTableByDalConfig(dalConfigXml));
        }

        // 3. 加载数据源bean
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("config/acts.xml");
        applicationContext.refresh();

        // 4. 获取db连接并执行表结构查询
        Connection conn = null;
        try {

            DataAccessConfigManager dataAccessConfigManager =
                    (DataAccessConfigManager) applicationContext.getBean("dataAccessConfigManager");

            for (String jdbc : dataAccessConfigManager.dataSourceMap.keySet()) {
                if (!jdbc.contains("direct")) {
                    continue;
                }

                conn = ((DataSource) (applicationContext.getBean(jdbc))).getConnection();
                for (String table : tableSet) {
                    genDBCSVFile(conn, table, dbType);
                }
            }

        } catch (Exception e) {
            TestifyLogUtil.fail(log, "Exception obtaining DB connection", e);
        }
    }

    /**
     * 生成单个表模版
     *
     * @param table
     * @param dbType
     */
    public static void generateSingleTableModel(String table, String dbType) {

        // 1. 加载数据源bean
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("config/acts.xml");
        applicationContext.refresh();

        // 2. 获取db连接并执行表结构查询
        Connection conn = null;
        try {
            Map<String, JdbcTemplate> jdbcMap =
                    ((DBDatasProcessor) applicationContext.getBean("dbDatasProcessor")).getJdbcTemplateMap();
            for (String jdbc : jdbcMap.keySet()) {

                conn = jdbcMap.get(jdbc).getDataSource().getConnection();

                genDBCSVFile(conn, table, dbType);
            }

        } catch (Exception e) {
            TestifyLogUtil.fail(log, "Exception obtaining DB connection", e);
        }
    }

    public static void genDBCSVFile(Connection conn, String table, String dbType) {

        List<Map<String, Object>> result = null;
        if (StringUtils.equals(dbType, "OB") || StringUtils.equals(dbType, "MYSQL")) {
            String sqlOBorMysql = "desc " + table;
            result = executeQuerySql(conn, sqlOBorMysql);
            if (result.size() == 0) {
                sqlOBorMysql = "desc " + table + "_000";
                result = executeQuerySql(conn, sqlOBorMysql);
            }

            if (result.size() == 0) {
                sqlOBorMysql = "desc " + table + "_00";
                result = executeQuerySql(conn, sqlOBorMysql);
            }
        }

        if (StringUtils.equals(dbType, "ORACLE")) {
            String sqlOracle = "SELECT A.COLUMN_NAME,"
                    + " DATA_TYPE||DECODE(DATA_TYPE,'DATE','','CLOB','','BLOB','','BFILE','','FLOAT','','LONG"
                    + " RAW','','LONG','','RAW','(' || TO_CHAR(DATA_LENGTH) || ')',"
                    + " (DECODE(SIGN(INSTR(DATA_TYPE, 'CHAR')),1, '(' || TO_CHAR(DATA_LENGTH)"
                    + " || ')',(DECODE(SUBSTR(DATA_TYPE, 1, 9), 'TIMESTAMP', '',"
                    + " (DECODE(NVL(DATA_PRECISION, -1), -1, '',(DECODE(NVL(DATA_SCALE, 0), 0,"
                    + " '(' || TO_CHAR(DATA_PRECISION) || ')', '(' || TO_CHAR(DATA_PRECISION)"
                    + " || ',' || TO_CHAR(DATA_SCALE) || ')'))))))))) , A.NULLABLE,"
                    + " A.DATA_DEFAULT , B.COMMENTS FROM ALL_TAB_COLUMNS A, ALL_COL_COMMENTS B"
                    + " WHERE B.TABLE_NAME=A.TABLE_NAME AND A.OWNER=B.OWNER AND"
                    + " A.COLUMN_NAME=B.COLUMN_NAME AND A.TABLE_NAME =UPPER('"
                    + table
                    + "') ORDER BY COLUMN_ID";
            result = executeQuerySql(conn, sqlOracle);
            if (result.size() == 0) {
                sqlOracle = "SELECT A.COLUMN_NAME,"
                        + " DATA_TYPE||DECODE(DATA_TYPE,'DATE','','CLOB','','BLOB','','BFILE','','FLOAT','','LONG"
                        + " RAW','','LONG','','RAW','(' || TO_CHAR(DATA_LENGTH) || ')',"
                        + " (DECODE(SIGN(INSTR(DATA_TYPE, 'CHAR')),1, '(' ||"
                        + " TO_CHAR(DATA_LENGTH) || ')',(DECODE(SUBSTR(DATA_TYPE, 1, 9),"
                        + " 'TIMESTAMP', '', (DECODE(NVL(DATA_PRECISION, -1), -1,"
                        + " '',(DECODE(NVL(DATA_SCALE, 0), 0, '(' || TO_CHAR(DATA_PRECISION) ||"
                        + " ')', '(' || TO_CHAR(DATA_PRECISION) || ',' || TO_CHAR(DATA_SCALE)"
                        + " || ')'))))))))) , A.NULLABLE, A.DATA_DEFAULT , B.COMMENTS FROM"
                        + " ALL_TAB_COLUMNS A, ALL_COL_COMMENTS B WHERE"
                        + " B.TABLE_NAME=A.TABLE_NAME AND A.OWNER=B.OWNER AND"
                        + " A.COLUMN_NAME=B.COLUMN_NAME AND A.TABLE_NAME =UPPER('"
                        + table
                        + "_00"
                        + "') ORDER BY COLUMN_ID";
                result = executeQuerySql(conn, sqlOracle);
            }

            if (result.size() == 0) {
                sqlOracle = "SELECT A.COLUMN_NAME,"
                        + " DATA_TYPE||DECODE(DATA_TYPE,'DATE','','CLOB','','BLOB','','BFILE','','FLOAT','','LONG"
                        + " RAW','','LONG','','RAW','(' || TO_CHAR(DATA_LENGTH) || ')',"
                        + " (DECODE(SIGN(INSTR(DATA_TYPE, 'CHAR')),1, '(' ||"
                        + " TO_CHAR(DATA_LENGTH) || ')',(DECODE(SUBSTR(DATA_TYPE, 1, 9),"
                        + " 'TIMESTAMP', '', (DECODE(NVL(DATA_PRECISION, -1), -1,"
                        + " '',(DECODE(NVL(DATA_SCALE, 0), 0, '(' || TO_CHAR(DATA_PRECISION) ||"
                        + " ')', '(' || TO_CHAR(DATA_PRECISION) || ',' || TO_CHAR(DATA_SCALE)"
                        + " || ')'))))))))) , A.NULLABLE, A.DATA_DEFAULT , B.COMMENTS FROM"
                        + " ALL_TAB_COLUMNS A, ALL_COL_COMMENTS B WHERE"
                        + " B.TABLE_NAME=A.TABLE_NAME AND A.OWNER=B.OWNER AND"
                        + " A.COLUMN_NAME=B.COLUMN_NAME AND A.TABLE_NAME =UPPER('"
                        + table
                        + "_000"
                        + "') ORDER BY COLUMN_ID";
                result = executeQuerySql(conn, sqlOracle);
            }
        }

        genDBCSVFile(TestifyPathConstants.DB_DATA_PATH + table + ".csv", result);
    }

    public static void genDBCSVFile(String csvPath, List<Map<String, Object>> map) {
        if (StringUtils.isBlank(csvPath)) {
            throw new TestifyException("Blank file path,cannot generate CSV");
        }
        File file = FileUtil.getTestResourceFile(csvPath);
        if (file.exists() || map.isEmpty()) {
            return;
        }

        List<String[]> outputValues = new ArrayList<String[]>();
        // 组装CSV文件第一行，标题行
        List<String> header = new ArrayList<String>();
        header.add(CSVColEnum.COLUMN.getCode());
        header.add(CSVColEnum.COMMENT.getCode());
        header.add(CSVColEnum.TYPE.getCode());
        header.add(CSVColEnum.FLAG.getCode());
        header.add("description");
        outputValues.add(header.toArray(new String[header.size()]));

        for (Map<String, Object> childMap : map) {
            List<String> value = new ArrayList<String>();
            String columnName = null;
            if (!(childMap.get("field") == null)) {
                // ob或mysql
                columnName = childMap.get("field").toString();
            } else {
                // oracle
                columnName = childMap.get("COLUMN_NAME").toString();
            }

            String comment = "";
            String columnType = "";
            String flag;
            if (childMap.get("key") != null) {
                boolean primaryFlag = Integer.valueOf(childMap.get("key").toString()) > 0 ? true : false;
                comment = childMap.get("comment").toString();
                columnType = childMap.get("type").toString();

                //                if (primaryFlag)
                //                    flag = "C";
                //                else if (columnType.equals("TIMESTAMP")) {
                //                    flag = "D";
                //                } else
                //                    flag = "Y";
            }

            // 默认都校验
            flag = "Y";

            value.add(columnName);
            value.add(comment);
            value.add(columnType);
            value.add(flag);
            if (flag.equals("D")) value.add("today");
            else if (columnName.equals("currency")) value.add("156");
            else value.add("");
            outputValues.add(value.toArray(new String[value.size()]));
        }
        writeToCsv(file, outputValues);
    }

    public static void writeToCsv(File file, List<String[]> outputValues) {

        // 初始化写入文件
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (Exception e) {
            // ActsLogUtil.fail(LOG, "初始化写入文件【" + file.getName() + "】失败", e);
        }
        // 将生成内容写入CSV文件
        try {
            OutputStreamWriter osw = null;
            osw = new OutputStreamWriter(outputStream);
            CSVWriter csvWriter = new CSVWriter(osw);
            csvWriter.writeAll(outputValues);
            csvWriter.close();
            TestifyLogUtil.warn(log, file.getName() + " successfully generated");
        } catch (Exception e) {
            TestifyLogUtil.fail(log, "Failed to write csv file:" + file.getName(), e);
        }
    }

    public static List<Map<String, Object>> executeQuerySql(Connection conn, String sql) {

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
            TestifyLogUtil.error(log, "Exception excuting sql, sql=" + sql, e);
        }
        return new ArrayList<Map<String, Object>>();
    }
}
