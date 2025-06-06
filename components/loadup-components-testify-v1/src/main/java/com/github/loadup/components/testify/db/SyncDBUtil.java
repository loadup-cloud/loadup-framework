/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.db;

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

import com.github.loadup.components.testify.db.enums.ColumnTypeEnum;
import com.github.loadup.components.testify.db.model.DBDataModel;
import com.github.loadup.components.testify.db.model.DBSyncModel;
import com.github.loadup.components.testify.helper.CSVHelper;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.util.FileUtil;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存配置表同步工具类，因sql不同，故仅支持mysql获取形式的数据库（OB，mysql）
 *
 *
 *
 */
@Slf4j
public class SyncDBUtil {

    /**
     * 从OB同步缓存表入本地csv文件（若文件已存在则按顺序合并）
     *
     * @param DBURL
     * @param cacheCSVFolder，test bundle resource目录下相对路径
     * @param tableNames
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public static void syncFromOB(String DBURL, String cacheCSVFolder, List<String> tableNames) {
        File file = FileUtil.getTestResourceFile(cacheCSVFolder);
        if (!file.exists() || !file.isDirectory()) file.mkdir();

        Map<String, List> fileMap = new HashMap<String, List>();
        for (File cacheFile : file.listFiles()) {
            String fileName =
                    cacheFile.getName().substring(0, cacheFile.getName().length() - 4);
            for (String cacheName : tableNames) {
                if (fileName.equals(cacheName)) {
                    try {
                        fileMap.put(cacheName, CSVHelper.readFromCsv(cacheFile));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        for (String cacheName : tableNames) {
            try {
                DBSyncModel dbModel = getSchema(cacheName);
                if (dbModel == null) continue;
                fillDBData(dbModel);

                fillCSVData(dbModel, fileMap.get(dbModel.getTableName()));
                mergeLocalData(dbModel);
                File csvFile = FileUtil.getTestResourceFile(cacheCSVFolder + "/" + dbModel.getTableName() + ".csv");
                genCheckCsvFile(dbModel, csvFile);
            } catch (Exception e) {
                log.error("OB获取数据异常", e);
            }
        }
    }

    /**
     * 同步本地所有缓存表入OB
     *
     * @param DBURL
     * @param cacheCSVFolder，test bundle resource目录下相对路径
     * @throws Exception
     */
    public static void syncAllTablesToOB(String DBURL, String cacheCSVFolder) throws Exception {
        updateAllTables(cacheCSVFolder);
    }

    /**
     * 同步本地缓存表指定行入OB
     *
     * @param DBURL
     * @param cacheCSVFolder，test bundle resource目录下相对路径
     * @param tableName
     * @throws Exception
     */
    public static void syncSelectedTableToOB(String DBURL, String cacheCSVFolder, String tableName) {
        updateSelectTable(cacheCSVFolder, tableName);
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
    public static void syncSelectedTableToOB(String DBURL, String cacheCSVFolder, String tableName, String cols)
            throws Exception {
        updateSelectTableWithCols(cacheCSVFolder, tableName, cols);
    }

    @SuppressWarnings("rawtypes")
    private static void updateSelectTable(String cacheCSVFolder, String cacheName) {
        try {
            List tableList = CSVHelper.readFromCsv(cacheCSVFolder + "/" + cacheName + ".csv");
            DBSyncModel dbModel = new DBSyncModel(cacheName);
            fillCSVData(dbModel, tableList);
            Map<String, DBDataModel> dbSchema = dbModel.getLocalSchema();

            for (Map<String, Object> dbData : dbModel.getLocalRowData()) {
                updateTable(cacheName, dbData, dbSchema, dbModel.getSchemaColumnList());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    private static void updateSelectTableWithCols(String cacheCSVFolder, String cacheName, String cols) {
        String[] columns = cols.split(",");

        try {
            List tableList = CSVHelper.readFromCsv(cacheCSVFolder + "/" + cacheName + ".csv");
            DBSyncModel dbModel = new DBSyncModel(cacheName);
            fillCSVData(dbModel, tableList);
            Map<String, DBDataModel> dbSchema = dbModel.getLocalSchema();
            for (String col : columns) {
                Map<String, Object> dbData = dbModel.getLocalRowData().get(Integer.valueOf(col) - 1);
                updateTable(cacheName, dbData, dbSchema, dbModel.getSchemaColumnList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    private static void updateAllTables(String cacheCSVFolder) {
        // 更新所有缓存表
        File file = FileUtil.getTestResourceFile(cacheCSVFolder);
        for (File cacheFile : file.listFiles()) {
            if (cacheFile.getName().endsWith(".csv")) {
                try {
                    String tableName =
                            cacheFile.getName().substring(0, cacheFile.getName().length() - 4);
                    List tableList = CSVHelper.readFromCsv(cacheFile);
                    DBSyncModel dbModel = new DBSyncModel(tableName);
                    fillCSVData(dbModel, tableList);
                    Map<String, DBDataModel> dbSchema = dbModel.getLocalSchema();

                    for (Map<String, Object> dbData : dbModel.getLocalRowData()) {
                        updateTable(tableName, dbData, dbSchema, dbModel.getSchemaColumnList());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void updateTable(
            String tableName, Map<String, Object> dbData, Map<String, DBDataModel> dbSchema, List<String> columnList)
            throws Exception {
        List<String> whereCondition = new ArrayList<String>();
        List<String> insertColumns = new ArrayList<String>();
        List<String> insertCondition = new ArrayList<String>();

        for (String columnName : columnList) {
            DBDataModel columnSchema = dbSchema.get(columnName);
            String columnType = columnSchema.getType();
            ColumnTypeEnum columnTypeEnum = ColumnTypeEnum.getByPrefix(columnType);
            String value;
            if (dbData.get(columnName) == null) {
                columnTypeEnum = ColumnTypeEnum.NULL;
                value = null;
            } else value = (String) (dbData.get(columnName));
            insertColumns.add(columnName);
            switch (columnTypeEnum) {
                case NULL:
                    insertCondition.add("null");
                    if (columnSchema.isPrimary()) whereCondition.add(String.format("%s is null", columnName));
                    break;
                case TIMESTAMP:
                case VARCHAR:
                    value = value.replace("'", "\\'");
                    insertCondition.add(String.format("'%s'", value));
                    if (columnSchema.isPrimary()) whereCondition.add(String.format("%s='%s'", columnName, value));
                    break;
                case INT:
                    insertCondition.add(value);
                    if (columnSchema.isPrimary()) whereCondition.add(String.format("%s=%s", columnName, value));
                    break;
                default:
                    throw new Exception("需要配置列种类枚举再执行");
            }
        }
        String delSql = "delete from " + tableName + " where " + joinString(whereCondition, " and ");
        String insertSql = "insert into " + tableName + "(" + joinString(insertColumns, ",") + ")" + " values("
                + joinString(insertCondition, ",") + ")";
        TestifyLogUtil.info(log, "开始执行" + delSql);
        int result = TestifyDBUtil.getUpdateResultMap(delSql, tableName, null);
        if (result < 0) {
            TestifyLogUtil.error(log, "影响行数错误:" + result + " - " + delSql);
        } else {
            TestifyLogUtil.info(log, "执行成功，影响行数:" + result);
        }
        TestifyLogUtil.info(log, "开始执行" + insertSql);
        result = TestifyDBUtil.getUpdateResultMap(insertSql, tableName, null);
        if (result != 1) {
            TestifyLogUtil.error(log, "影响行数错误:" + result + " - " + insertSql);
        } else {
            TestifyLogUtil.info(log, "执行成功，影响行数:" + result);
        }
    }

    private static void fillDBData(DBSyncModel dbModel) throws Exception {
        String tableName = dbModel.getTableName();
        List<Map<String, Object>> results =
                TestifyDBUtil.getMultiQueryResultMap("select * from " + tableName + " order by id", tableName, null);
        if (results != null) {
            List<Map<String, Object>> dbRowData = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> resultObj : results) {
                dbRowData.add(resultObj);
            }
            dbModel.setDbRowData(dbRowData);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void fillCSVData(DBSyncModel dbModel, List tableList) {
        if (tableList == null) return;
        List<Map<String, Object>> rowDataList = new ArrayList<Map<String, Object>>();
        Map<String, DBDataModel> localSchema = new HashMap<String, DBDataModel>();
        List<String> schemaColumnList = new ArrayList<String>();
        int rowLength = ((String[]) tableList.get(0)).length;
        for (int index = 1; index < tableList.size(); index++) {
            String[] data = (String[]) tableList.get(index);
            String columnName = data[0];
            boolean isPrimary = data[3].equals("Y") ? true : false;
            boolean isUnique = data[4].equals("Y") ? true : false;
            DBDataModel model = new DBDataModel(data[0], data[1], isPrimary, isUnique);
            localSchema.put(columnName, model);
            schemaColumnList.add(columnName);
        }

        for (int dataIndex = 5; dataIndex < rowLength; dataIndex++) {
            Map<String, Object> rowData = new HashMap<String, Object>();
            for (int columnIndex = 1; columnIndex < tableList.size(); columnIndex++) {
                String[] data = (String[]) tableList.get(columnIndex);
                if (data[dataIndex].equals("null")) rowData.put(data[0], null);
                else rowData.put(data[0], data[dataIndex]);
            }
            rowDataList.add(rowData);
        }
        dbModel.setLocalSchema(localSchema);
        dbModel.setLocalRowData(rowDataList);
        if (!schemaColumnList.isEmpty()) dbModel.setSchemaColumnList(schemaColumnList);
    }

    private static DBSyncModel getSchema(String tableName) throws Exception {
        DBSyncModel dbModel = new DBSyncModel(tableName);
        List<Map<String, Object>> results =
                TestifyDBUtil.getMultiQueryResultMap(String.format("show index from %s", tableName), tableName, null);

        Set<String> uniqueKeySet = new HashSet<String>();
        for (Map<String, Object> result : results) {
            uniqueKeySet.add(result.get("column_name").toString());
        }
        dbModel.setUniqueKeySet(uniqueKeySet);

        results = TestifyDBUtil.getMultiQueryResultMap("desc " + tableName, tableName, null);
        if (results != null) {
            Map<String, DBDataModel> dbSchema = new HashMap<String, DBDataModel>();
            List<String> schemaColumnList = new ArrayList<String>();
            for (Map<String, Object> result : results) {
                String columnName = result.get("field").toString();
                String columnType = result.get("type").toString();
                boolean isPrimary = Integer.valueOf(result.get("key").toString()) > 0 ? true : false;
                boolean isUnique = uniqueKeySet.contains(columnName) ? true : false;
                String comment = result.get("comment").toString();
                DBDataModel model = new DBDataModel(comment, columnType, isPrimary, isUnique);
                dbSchema.put(columnName, model);
                schemaColumnList.add(columnName);
            }
            dbModel.setDbSchema(dbSchema);
            dbModel.setSchemaColumnList(schemaColumnList);
        } else return null;
        return dbModel;
    }

    private static void genCheckCsvFile(DBSyncModel dbModel, File csvFile) {
        if (csvFile.exists() && !csvFile.delete()) {
            log.error("无法删除文件" + csvFile.getName());
            return;
        }

        List<String[]> outputValues = new ArrayList<String[]>();
        // 组装CSV文件第一行，标题行
        List<String> header = new ArrayList<String>();
        header.add("Name");
        header.add("Type");
        header.add("Comment");
        header.add("isPrimary");
        header.add("isUnique");
        for (int i = 1; i < dbModel.getMergeRowData().size() + 1; i++) {
            header.add(String.valueOf(i));
        }
        outputValues.add(header.toArray(new String[header.size()]));

        for (String columnName : dbModel.getSchemaColumnList()) {
            List<String> value = new ArrayList<String>();
            value.add(columnName);
            DBDataModel model = dbModel.getDbSchema().get(columnName);
            value.add(model.getType());
            value.add(model.getComment());
            value.add(model.isPrimary() ? "Y" : "N");
            value.add(model.isUnique() ? "Y" : "N");
            for (Map<String, Object> dbData : dbModel.getMergeRowData()) {
                Object data = dbData.get(columnName);
                value.add(data == null ? "null" : data.toString());
            }
            outputValues.add(value.toArray(new String[value.size()]));
        }
        CSVHelper.writeToCsv(csvFile, outputValues);
    }

    private static String joinString(List<String> stringList, String joinKey) {
        String tmp = "";
        for (String element : stringList) {
            tmp += element + joinKey;
        }
        return tmp.substring(0, tmp.length() - joinKey.length());
    }

    private static void mergeLocalData(DBSyncModel dbModel) {
        List<Map<String, Object>> mergeRowData = new ArrayList<Map<String, Object>>();
        Map<String, Map<String, Object>> uniqueMap = new HashMap<String, Map<String, Object>>();

        // 将数据库数据放入uniqueMap
        for (Map<String, Object> dbData : dbModel.getDbRowData()) {
            uniqueMap.put(genUniqueKey(dbModel, dbData), dbData);
        }

        // 将缓存表存在、数据库不存在的的数据放入uniqueMap，并按缓存表旧数据顺序逐个添加mergeRowData
        if (dbModel.getLocalRowData().size() != 0) {
            for (Map<String, Object> dbData : dbModel.getLocalRowData()) {
                String uniqueKey = genUniqueKey(dbModel, dbData);
                if (uniqueMap.containsKey(uniqueKey)) {
                    log.info(String.format("数据%s已在表%s中,进行更新", dbData.toString(), dbModel.getTableName()));
                    mergeRowData.add(uniqueMap.get(uniqueKey));
                    uniqueMap.remove(uniqueKey);
                } else mergeRowData.add(dbData);
            }
        }

        // 将数据库多出来的数据保持顺序逐个添加到mergeRowData
        for (Map<String, Object> dbData : dbModel.getDbRowData()) {
            if (uniqueMap.containsKey(genUniqueKey(dbModel, dbData))) {
                mergeRowData.add(dbData);
            }
        }

        dbModel.setMergeRowData(mergeRowData);
    }

    private static String genUniqueKey(DBSyncModel dbModel, Map<String, Object> dbData) {
        String uniqueKey = "";
        for (Entry<String, DBDataModel> entry : dbModel.getDbSchema().entrySet()) {
            if (entry.getValue().isUnique() || entry.getValue().isPrimary())
                uniqueKey += "-" + dbData.get(entry.getKey());
        }
        return uniqueKey.substring(1, uniqueKey.length());
    }
}
