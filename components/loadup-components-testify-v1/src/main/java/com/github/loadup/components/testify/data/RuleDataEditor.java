/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.data;

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

import com.github.loadup.components.testify.constant.TestifyDBConstants;
import com.github.loadup.components.testify.db.offlineService.AbstractDBService;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.helper.CSVHelper;
import com.github.loadup.components.testify.manage.TestLogger;
import com.github.loadup.components.testify.util.FileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import org.apache.commons.lang3.StringUtils;

/**
 * 规则数据编辑器。
 * <p>
 * 采取乐观版本控制方式，表级锁。
 *
 *
 *
 */
public class RuleDataEditor {

    /**
     * 版本控制
     */
    private static final Properties versions;

    /**
     * db服务
     */
    private static final AbstractDBService dbService;

    static {
        AbstractDBService dbs = null;
        try {
            dbs = AbstractDBService.getService(
                    TestifyDBConstants.DB_URL,
                    TestifyDBConstants.DB_USER_NAME,
                    TestifyDBConstants.DB_PASSWORD,
                    TestifyDBConstants.DB_SCHEMA);
        } catch (Exception e) {
            dbs = null;
            TestLogger.getLogger().error(e, "db access error,url=" + TestifyDBConstants.DB_URL);
        }

        dbService = dbs;
    }

    static {
        Properties vers = new Properties();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(FileUtil.getTestResourceFile("ruledata/versions.properties"));
            vers.load(inputStream);
        } catch (Exception e) {
            TestLogger.getLogger().warn("can't load file: ruledata/versions.properties");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        }
        versions = vers;
    }

    /**
     * 根据条件装载某表数据，返回当前数据版本号。
     *
     * @param tableName
     * @param condition
     * @return
     */
    public int load(String tableName, String condition) {
        List<String[]> csvData = new ArrayList<String[]>();

        String[] header = getHeader(tableName);
        csvData.add(header);

        String sql = "SELECT * FROM " + tableName;
        if (!StringUtils.isEmpty(condition)) {
            sql = sql + " WHERE " + condition;
        }

        List<Map<String, Object>> datas = dbService.executeQuerySql(sql);
        for (Map<String, Object> data : datas) {
            String[] rowData = new String[header.length];
            for (int i = 0; i < header.length; i++) {
                rowData[i] = (String) data.get(header[i]);
            }
            csvData.add(rowData);
        }

        CSVHelper.writeToCsv(getCSVFileName(tableName), csvData);

        return 0;
    }

    /**
     * @param tableName
     * @return
     */
    private File getCSVFileName(String tableName) {
        return FileUtil.getTestResourceFile("ruledata/" + tableName + ".csv");
    }

    /**
     * 根据key保存数据。如果key为空，则通过主键操作。
     *
     * @param tableName
     * @param keys
     * @return
     */
    @SuppressWarnings("unchecked")
    public int save(String tableName, String... keys) {
        if (keys == null || keys.length == 0) {
            keys = getPrimaryKey(tableName);
        }
        if (keys == null || keys.length == 0) {
            throw new TestifyException("can't find primary key[table=" + tableName + "]");
        }
        List<String[]> csvData = CSVHelper.readFromCsv(getCSVFileName(tableName));

        if (csvData == null || csvData.isEmpty()) {
            throw new TestifyException("table file's content not right[table=" + tableName + "]");
        }

        Map<String, Integer> indexes = new HashMap<String, Integer>();
        List<String> updateColumns = new ArrayList<String>();

        String[] header = csvData.get(0);
        for (int i = 0; i < header.length; i++) {
            indexes.put(header[i], i);
            if (!contains(keys, header[i])) {
                updateColumns.add(header[i]);
            }
        }

        for (int i = 1; i < csvData.size(); i++) {
            int count = doUpdate(tableName, csvData.get(i), indexes, updateColumns, keys);

            if (count <= 0) {
                doInsert(tableName, csvData.get(i), header);
            }
        }

        return 0;
    }

    /**
     * @param tableName
     * @param rowData
     * @param header
     */
    private void doInsert(String tableName, String[] rowData, String[] header) {
        StringBuilder insertSql = new StringBuilder("insert into ");
        insertSql.append(tableName);
        insertSql.append("(");
        for (String c : header) {
            insertSql.append(c);
            insertSql.append(",");
        }
        insertSql.deleteCharAt(insertSql.length() - 1);
        insertSql.append(") values (");
        for (String v : rowData) {
            insertSql.append("'");
            insertSql.append(v);
            insertSql.append("',");
        }
        insertSql.deleteCharAt(insertSql.length() - 1);
        insertSql.append(")");
        dbService.executeUpdateSql(insertSql.toString());
    }

    /**
     * @param tableName
     * @param rowData
     * @param indexes
     * @param updateColumns
     * @param keys
     * @return
     */
    private int doUpdate(
            String tableName,
            String[] rowData,
            Map<String, Integer> indexes,
            List<String> updateColumns,
            String... keys) {
        StringBuilder updateSql = new StringBuilder("update ");
        updateSql.append(tableName);
        updateSql.append(" set ");
        for (String col : updateColumns) {
            updateSql.append(col);
            updateSql.append("='");
            updateSql.append(rowData[indexes.get(col)]);
            updateSql.append("',");
        }
        updateSql.deleteCharAt(updateSql.length() - 1);
        updateSql.append(" where ");
        for (String key : keys) {
            updateSql.append(key);
            updateSql.append("='");
            updateSql.append(rowData[indexes.get(key)]);
            updateSql.append("' and ");
        }
        updateSql.delete(updateSql.length() - 5, updateSql.length());

        int count = dbService.executeUpdateSql(updateSql.toString());
        return count;
    }

    /**
     * @param keys
     * @param key
     * @return
     */
    private boolean contains(String[] keys, String key) {
        boolean exist = false;
        for (String k : keys) {
            if (k.equals(key)) {
                return true;
            }
        }
        return exist;
    }

    /**
     * 获取表头。
     *
     * @param tableName
     */
    private String[] getHeader(String tableName) {

        List<Map<String, Object>> columns = dbService.getTableInfo(tableName);
        List<String> header = new ArrayList<String>();

        for (Map<String, Object> col : columns) {
            String type = (String) col.get("Type");
            if (type.startsWith("varchar") || type.startsWith("char")) {
                header.add((String) col.get("Field"));
            }
        }

        return header.toArray(new String[0]);
    }

    /**
     * 获取表头。
     *
     * @param tableName
     */
    private String[] getPrimaryKey(String tableName) {

        List<Map<String, Object>> columns = dbService.getTableInfo(tableName);
        List<String> keys = new ArrayList<String>();

        for (Map<String, Object> col : columns) {
            if ("PRI".equalsIgnoreCase((String) col.get("Key"))) {
                keys.add((String) col.get("Field"));
            }
        }
        return keys.toArray(new String[0]);
    }
}
