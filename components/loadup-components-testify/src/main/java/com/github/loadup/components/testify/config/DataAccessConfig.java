package com.github.loadup.components.testify.config;

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

public class DataAccessConfig {

    private String dataSourceName;
    /**
     * 表名
     */
    private String tableName;
    /**
     * db表对应的主键
     */
    private String[] keys;
    /**
     * 数据比对查询时所用到的字段名称
     */
    private String[] selectKeys;

    public DataAccessConfig(String tableName, String[] keys, String[] selectKeys) {

        this.tableName = tableName;
        this.keys = keys;
        this.selectKeys = selectKeys;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public String[] getSelectKeys() {
        return selectKeys;
    }

    public void setSelectKeys(String[] selectKeys) {
        this.selectKeys = selectKeys;
    }

    @Override
    public String toString() {
        String message = "tableName" + this.tableName;
        if (this.keys.length > 0) {
            message += ",keys:";
            for (String key : this.keys) {
                message += key + "/";
            }
        }
        if (this.selectKeys.length > 0) {
            message += ",selectKeys为：";
            for (String key : this.selectKeys) {
                message += key + "/";
            }
        }
        return message;
    }
}
