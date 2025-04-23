package com.alipay.test.acts.util;

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

import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.constant.ActsPathConstants;
import com.alipay.test.acts.db.enums.CSVColEnum;
import com.alipay.test.acts.exception.ActsException;
import com.alipay.test.acts.helper.CSVHelper;
import com.alipay.test.acts.log.ActsLogUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

/**
 * 数据模版生成工具
 * @author xiaoleicxl
 * @version $Id: dbTableModelUtil.java, v 0.1 2015年10月22日 下午10:20:08 xiaoleicxl Exp $
 */
public class dbTableModelUtil {

    private static final Log LOG = LogFactory.getLog(CSVHelper.class);



    /**
     * 生成单个表模版
     * @param table
     * @param dbType "ORACLE"\"OB"\"MYSQL"三种
     */
    public static void genDBCSVFile(String csvModelRootPath, Connection conn, String table,
                                    String dataSql, String dbType, String encode) {

        if (StringUtils.equals(dbType, "OB") || StringUtils.equals(dbType, "MYSQL")) {

            List<Map<String, Object>> result = null;
            List<Map<String, Object>> dataResult = null;
            String sqlOBorMysql = "desc " + table;
            result = executeQuerySql(conn, sqlOBorMysql);
            if (StringUtils.isNotBlank(dataSql)) {
                dataResult = executeQuerySql(conn, dataSql);
            }
            if (result.isEmpty()) {
                throw new ActsException(table + "生成模版失败！请检查查询sql是否正确");
            }

            //获取物理表名，不带分库分表
            String tableName = table;
            while (StringUtils.isNumeric(
                    StringUtils.substring(tableName, tableName.length() - 1, tableName.length()))) {
                tableName = StringUtils.substringBeforeLast(tableName, "_");
            }

            File dbModel = FileUtil.getTestResourceFileByRootPath(
                    csvModelRootPath + "/" + ActsPathConstants.DB_DATA_PATH);
            if (!dbModel.exists())
                dbModel.mkdir();

            genMysqlDBCSVFile(
                    csvModelRootPath + "/" + ActsPathConstants.DB_DATA_PATH + tableName + ".csv",
                    result, dataResult, encode);

        }

        if (StringUtils.equals(dbType, "ORACLE")) {

            List<Map<String, Object>> dataResult = null;
            Map<String, Map<String, String>> result = null;
            String sqlOracle = "select COLUMN_NAME,DATA_TYPE,DATA_LENGTH,NULLABLE　from user_tab_columns where table_name =UPPER('"
                    + table + "')";

            result = getOracleColumnInfo(conn, sqlOracle);
            if (StringUtils.isNotBlank(dataSql)) {
                dataResult = executeQuerySql(conn, dataSql);
            }

            if (result.isEmpty()) {
                sqlOracle = "select COLUMN_NAME,DATA_TYPE,DATA_LENGTH,NULLABLE　from all_tab_columns where table_name =UPPER('"
                        + table + "')";
                result = getOracleColumnInfo(conn, sqlOracle);
            }
            //获取物理表名，不带分库分表
            String tableName = table;
            while (StringUtils.isNumeric(
                    StringUtils.substring(tableName, tableName.length() - 1, tableName.length()))) {
                tableName = StringUtils.substringBeforeLast(tableName, "_");
            }

            genOracleDBCSVFile(
                    csvModelRootPath + "/" + ActsPathConstants.DB_DATA_PATH + tableName + ".csv",
                    result, dataResult, encode);

        }

    }

    public static void genMysqlDBCSVFile(String csvRootPath, List<Map<String, Object>> map,
                                         List<Map<String, Object>> dataMap, String encode) {
        if (StringUtils.isBlank(csvRootPath)) {
            throw new ActsException("路径为空，无法生成CSV文件");
        }
        File file = FileUtil.getTestResourceFileByRootPath(csvRootPath);

        //Assert.assertTrue("文件【" + csvPath + "】已经存在，请尝试输入新文件路径", !file.exists());
        if (file.exists() || map.isEmpty()) {
            return;
        }
        List<String[]> outputValues = new ArrayList<String[]>();
        //组装CSV文件第一行，标题行
        List<String> header = new ArrayList<String>();
        header.add(CSVColEnum.COLUMN.getCode());
        header.add(CSVColEnum.COMMENT.getCode());
        header.add(CSVColEnum.TYPE.getCode());
        header.add(CSVColEnum.RULE.getCode());
        header.add(CSVColEnum.FLAG.getCode());

        //有DB值就添加，无默认返回
        if (dataMap != null && !dataMap.isEmpty()) {
            for (int i = 1; i < dataMap.size() + 1; i++) {
                header.add(CSVColEnum.VALUE.getCode() + i);
            }
        } else {
            header.add(CSVColEnum.VALUE.getCode());
        }
        outputValues.add(header.toArray(new String[header.size()]));

        for (Map<String, Object> childMap : map) {
            List<String> value = new ArrayList<String>();
            String columnName = null;
            if (!(childMap.get("field") == null)) {
                //ob
                columnName = childMap.get("field").toString();
            } else if (!(childMap.get("Field") == null)) {
                //mysql
                columnName = childMap.get("Field").toString();
            }
            String comment = "";
            String columnType = "";
            String columnRule = "";
            String flag;
            if (childMap.get("key") != null) {
                comment = childMap.get("comment")==null?"":childMap.get("comment").toString();
                columnType = childMap.get("type")==null?"":childMap.get("type").toString();

            }else if (childMap.get("Key") != null) {
                comment = childMap.get("Comment")==null?"":childMap.get("Comment").toString();
                columnType = childMap.get("Type")==null?"":childMap.get("Type").toString();

            }

            //默认都校验
            flag = "Y";

            value.add(columnName);
            value.add(comment);
            value.add(columnType);
            value.add(columnRule);
            value.add(flag);
            if (dataMap != null) {
                for (Map<String, Object> cloumValue : dataMap) {
                    if(null!=cloumValue.get(columnName)){
                        value.add((String) cloumValue.get(columnName).toString());
                    }else{
                        value.add("");
                    }

                }
            } else {
                if (columnName.equals("currency")) {
                    value.add("156");
                } else {
                    value.add("");
                }
            }
            outputValues.add(value.toArray(new String[value.size()]));

        }
        writeToCsv(file, outputValues, encode);
    }

    public static void genOracleDBCSVFile(String csvRootPath, Map<String, Map<String, String>> map,
                                          List<Map<String, Object>> dataMap, String encode) {
        if (StringUtils.isBlank(csvRootPath)) {
            throw new ActsException("路径为空，无法生成CSV文件");
        }
        File file = FileUtil.getTestResourceFileByRootPath(csvRootPath);

        //Assert.assertTrue("文件【" + csvPath + "】已经存在，请尝试输入新文件路径", !file.exists());
        if (file.exists() || map.isEmpty()) {
            return;
        }
        List<String[]> outputValues = new ArrayList<String[]>();
        //组装CSV文件第一行，标题行
        List<String> header = new ArrayList<String>();
        header.add(CSVColEnum.COLUMN.getCode());
        header.add(CSVColEnum.COMMENT.getCode());
        header.add(CSVColEnum.TYPE.getCode());
        header.add(CSVColEnum.RULE.getCode());
        header.add(CSVColEnum.FLAG.getCode());
        //有DB值就添加，无默认返回
        if (dataMap != null && !dataMap.isEmpty()) {
            for (int i = 1; i < dataMap.size() + 1; i++) {
                header.add(CSVColEnum.VALUE.getCode() + i);
            }
        } else {
            header.add(CSVColEnum.VALUE.getCode());
        }
        outputValues.add(header.toArray(new String[header.size()]));

        for (String column : map.keySet()) {
            List<String> value = new ArrayList<String>();
            String columnName = column;
            String comment = "";
            String columnType = map.get(column).get("DATA_TYPE") + "("
                    + map.get(column).get("DATA_LENGTH") + ")";
            String columnRule = "";
            String flag = "Y";//默认都校验

            value.add(columnName);
            value.add(comment);
            value.add(columnType);
            value.add(columnRule);
            value.add(flag);
            if (dataMap != null) {
                for (Map<String, Object> cloumValue : dataMap) {
                    if(null!=cloumValue.get(columnName)){
                        value.add((String) cloumValue.get(columnName).toString());
                    }else{
                        value.add("");
                    }
                }
            } else {
                if (columnName.equals("currency")) {
                    value.add("156");
                } else {
                    value.add("");
                }
            }
            outputValues.add(value.toArray(new String[value.size()]));

        }
        writeToCsv(file, outputValues, encode);
    }

    public static void writeToCsv(File file, List<String[]> outputValues, String encode) {

        //初始化写入文件
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (Exception e) {
            //ActsLogUtil.fail(LOG, "初始化写入文件【" + file.getName() + "】失败", e);
        }
        //将生成内容写入CSV文件
        try {
            OutputStreamWriter osw = null;
            osw = new OutputStreamWriter(outputStream, Charset.forName(encode));
            CSVWriter csvWriter = new CSVWriter(osw);
            csvWriter.writeAll(outputValues);
            csvWriter.close();
            ActsLogUtil.warn(LOG, file.getName() + "生成完毕");
        } catch (Exception e) {
            ActsLogUtil.fail(LOG, "通过文件流输出数据失败:" + file.getName(), e);
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
            ActsLogUtil.error(LOG, "执行sql异常，sql=" + sql, e);
        }
        return new ArrayList<Map<String, Object>>();
    }

    public static Map<String, Map<String, String>> getOracleColumnInfo(Connection con, String sql) {

        Statement st = null;
        Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Map<String, String> cloumMap = new LinkedHashMap<String, String>();
                String key = rs.getString("COLUMN_NAME"); //获取字段名
                cloumMap.put("DATA_TYPE", rs.getString("DATA_TYPE")); //获取数据类型
                cloumMap.put("DATA_LENGTH", rs.getString("DATA_LENGTH")); //获取数据长度
                cloumMap.put("NULLABLE", rs.getString("NULLABLE")); //获取是否为空
                map.put(key, cloumMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;

    }

    static void genDOCSVFile(String testResourcePath, ClassLoader clsLoader,
                             String fullClassName,String encode){

        try {
            if (StringUtils.isBlank(testResourcePath)) {
                ActsLogUtil.warn(LOG, "路径为空，无法生成CSV文件");
                return;
            }


            //判断dbMode 文件夹是否存在，不存在则创建
            File dbModel = FileUtil.getTestResourceFileByRootPath(
                    testResourcePath + "/" + ActsPathConstants.DB_DATA_PATH);
            if (!dbModel.exists())
                dbModel.mkdir();



            String tableName =StringUtils.substringAfterLast(fullClassName,".");
            String csvModelRootPath = testResourcePath + "/" + ActsPathConstants.DB_DATA_PATH + tableName + ".csv";

            File file = FileUtil.getTestResourceFileByRootPath(csvModelRootPath);


            if (null == fullClassName) {
                ActsLogUtil.warn(LOG, "DO类名为空，无法生成CSV文件");
                return;
            }

            if (file.exists()) {
                ActsLogUtil.warn(LOG, "文件【" + csvModelRootPath + "】已经存在，直接跳过");
                return;
            }

            Class<?> cls = clsLoader.loadClass(fullClassName);
            Field[] classFields = cls.getDeclaredFields();

            List<String[]> outputValues = new ArrayList<String[]>();
            //组装CSV文件第一行，标题行
            List<String> header = new ArrayList<String>();
            header.add(CSVColEnum.COLUMN.getCode());
            header.add(CSVColEnum.COMMENT.getCode());
            header.add(CSVColEnum.TYPE.getCode());
            header.add(CSVColEnum.RULE.getCode());
            header.add(CSVColEnum.FLAG.getCode());
            header.add(CSVColEnum.VALUE.getCode());

            outputValues.add(header.toArray(new String[header.size()]));

            for (Field field : classFields) {
                List<String> value = new ArrayList<String>();


                String fieldName = field.getName();
                StringBuilder builder=new StringBuilder("");
                for(int i =0;i<fieldName.length();i++) {
                    char c = fieldName.charAt(i);
                    if (Character.isUpperCase(c)) {
                        builder.append("_" + StringUtils.toRootLowerCase(String.valueOf(c)));

                    } else {
                        builder.append(String.valueOf(c));
                    }
                }
                //COLUMN
                value.add(builder.toString());
                //COMMENT
                value.add("");
                //TYPE
                value.add("");
                //RULE
                value.add("");
                //FLAG
                value.add("Y");
                //VALUE
                value.add("");

                outputValues.add(value.toArray(new String[value.size()]));

            }

            writeToCsv(file, outputValues, encode);

        }catch(ClassNotFoundException e){

            e.printStackTrace();
        }
    }





}
