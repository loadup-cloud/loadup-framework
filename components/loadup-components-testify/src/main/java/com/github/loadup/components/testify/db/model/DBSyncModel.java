/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.db.model;

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

import java.util.*;

/**
 * 数据库同步模型
 *
 *
 *
 */
public class DBSyncModel {
    public List<Map<String, Object>> localRowData = new ArrayList<Map<String, Object>>();

    public Map<String, DBDataModel> localSchema = new HashMap<String, DBDataModel>();

    public List<Map<String, Object>> dbRowData;

    public Map<String, DBDataModel> dbSchema;

    public List<String> schemaColumnList;

    public String tableName;
    public List<Map<String, Object>> mergeRowData;
    private Set<String> uniqueKeySet;

    public DBSyncModel(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Map<String, Object>> getLocalRowData() {
        return localRowData;
    }

    public void setLocalRowData(List<Map<String, Object>> localRowData) {
        this.localRowData = localRowData;
    }

    public Map<String, DBDataModel> getLocalSchema() {
        return localSchema;
    }

    public void setLocalSchema(Map<String, DBDataModel> localSchema) {
        this.localSchema = localSchema;
    }

    public List<Map<String, Object>> getDbRowData() {
        return dbRowData;
    }

    public void setDbRowData(List<Map<String, Object>> dbRowData) {
        this.dbRowData = dbRowData;
    }

    public Map<String, DBDataModel> getDbSchema() {
        return dbSchema;
    }

    public void setDbSchema(Map<String, DBDataModel> dbSchema) {
        this.dbSchema = dbSchema;
    }

    public Set<String> getUniqueKeySet() {
        return uniqueKeySet;
    }

    public void setUniqueKeySet(Set<String> uniqueKeySet) {
        this.uniqueKeySet = uniqueKeySet;
    }

    public List<Map<String, Object>> getMergeRowData() {
        return mergeRowData;
    }

    public void setMergeRowData(List<Map<String, Object>> mergeRowData) {
        this.mergeRowData = mergeRowData;
    }

    /**
     *
     *
     * @return property value of schemaColumnList
     */
    public List<String> getSchemaColumnList() {
        return schemaColumnList;
    }

    /**
     *
     *
     * @param schemaColumnList value to be assigned to property schemaColumnList
     */
    public void setSchemaColumnList(List<String> schemaColumnList) {
        this.schemaColumnList = schemaColumnList;
    }
}
