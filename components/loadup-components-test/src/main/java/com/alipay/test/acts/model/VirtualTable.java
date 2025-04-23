package com.alipay.test.acts.model;

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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 虚拟表
 */
public class VirtualTable extends TestNode {

    /** 如果是DO，则为DO的类名 */
    private String                                          dataObjClazz;

    /** 表名 */
    private String                                          tableName;

    /** 表模板的描述 */
    private String                                          tableBaseDesc;

    /** table rowRecords */
    private List<Map<String, Object>>                       tableData;

    /** flags */
    private Map<String /* fieldName*/, String /* Flag */> flags;

    public VirtualTable() {
    }

    public VirtualTable(String dataObjClazz) {
        this.dataObjClazz = dataObjClazz;
    }

    public VirtualTable(String tableName, String tableBaseDesc) {
        this.tableName = tableName;
        this.tableBaseDesc = tableBaseDesc;
    }

    public VirtualTable(Object dataObject) {
        addRow(dataObject);
    }

    /**
     * 获取表字段对应的flag，兼容表字段名称大小写
     * @param fieldName
     * @return flag标识
     */
    public String getFlagByFieldNameIgnoreCase(String fieldName) {
        if (this.flags == null || this.flags.isEmpty()) {
            return null;
        }

        String currentFlag = this.flags.get(fieldName); // 尝试原始字段名称
        if (currentFlag == null) {
            currentFlag = this.flags.get(fieldName.toLowerCase()); // 尝试原始字段小写
            if (currentFlag == null) {
                currentFlag = this.flags.get(fieldName.toUpperCase()); // 尝试原始字段大写
            }
        }

        return currentFlag;

    }

    /**
     * 通过map增加一行
     * @param row
     * @return
     */
    public VirtualTable addRow(Map<String, Object> row) {
        if (tableData == null) {
            tableData = new ArrayList<Map<String, Object>>();
        }
        this.tableData.add(row);
        return this;
    }

    /**
     * 通过map增加多行
     * @param rows
     * @return
     */
    public VirtualTable addRows(List<Map<String, Object>> rows) {
        if (tableData == null) {
            tableData = new ArrayList<Map<String, Object>>();
        }
        this.tableData.addAll(rows);
        return this;
    }

    /**
     * 通过DO增加一行
     * @param dataObject
     * @return
     */
    public VirtualTable addRow(Object dataObject) {
        if (dataObjClazz == null) {
            dataObjClazz = dataObject.getClass().getName();
        }
        if (tableData == null) {
            tableData = new ArrayList<Map<String, Object>>();
        }
        Map<String, Object> rowData = new LinkedHashMap<String, Object>();
        List<Field> tmpFields = new ArrayList<Field>();
        for (@SuppressWarnings("rawtypes")
        Class clazz = dataObject.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field f : clazz.getDeclaredFields()) {
                tmpFields.add(f);
            }
        }

        List<Field> fields = filterSyntheticFields(tmpFields);
        fields = filterStaticFields(fields);
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                rowData.put(field.getName(), field.get(dataObject));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        tableData.add(rowData);
        return this;
    }

    public Map<String, Object> getRow(int rowNum) {
        if (tableData == null || tableData.size() < rowNum + 1) {
            return null;
        }
        return tableData.get(rowNum);
    }

    /**
     * 更新指定行
     * 
     * @param rowNum
     * @param dataObj
     * @return
     */
    public VirtualTable setRow(int rowNum, Map<String, Object> row) {
        Map<String, Object> oldRow = getRow(rowNum);
        if (oldRow == null) {
            return this;
        }
        tableData.set(rowNum, row);
        return this;
    }

    public Integer getRecordNum() {
        return null == this.tableData ? 0 : this.tableData.size();
    }

    private List<Field> filterSyntheticFields(List<Field> tmpFields) {
        // 过滤 synthetic 属性
        List<Field> fieldsList = new ArrayList<Field>();
        for (Field field : tmpFields) {
            if (!field.isSynthetic()) {
                fieldsList.add(field);
            }
        }
        return fieldsList;
    }

    private List<Field> filterStaticFields(List<Field> tmpFields) {
        List<Field> fieldsList = new ArrayList<Field>();
        for (Field field : tmpFields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fieldsList.add(field);
            }
        }
        return fieldsList;
    }

    public String getDataObjClazz() {
        return dataObjClazz;
    }

    public void setDataObjClazz(String dataObjClazz) {
        this.dataObjClazz = dataObjClazz;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Map<String, Object>> getTableData() {
        return tableData;
    }

    public void setTableData(List<Map<String, Object>> tableData) {
        this.tableData = tableData;
    }

    public Map<String, String> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, String> flags) {
        this.flags = flags;
    }

    public String getTableBaseDesc() {
        return tableBaseDesc;
    }

    public void setTableBaseDesc(String tableBaseDesc) {
        this.tableBaseDesc = tableBaseDesc;
    }

    public VirtualTable setFlag(String key, String flag) {
        if (this.flags == null) {
            this.flags = new LinkedHashMap<String, String>();
        }
        this.flags.put(key, flag);
        return this;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "VirtualTable [dataObjClazz=" + dataObjClazz + ", tableName=" + tableName
               + ", tableBaseDesc=" + tableBaseDesc + ", tableData=" + tableData + ", flags="
               + flags + "]";
    }

}
