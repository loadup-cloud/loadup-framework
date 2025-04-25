package com.github.loadup.components.testify.template;

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

import com.github.loadup.components.testify.config.DataAccessConfig;
import com.github.loadup.components.testify.config.DataAccessConfigManager;
import com.github.loadup.components.testify.model.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrepareTemplateImpl implements PrepareTemplate {

    protected DataAccessConfigManager dataAccessConfigManager;

    @Override
    public PrepareData prepare(PrepareCallBack callback) {
        PrepareData data = new PrepareData();
        // 前置的db数据准备
        VirtualDataSet depDataSet = callback.prepareDepDataSet();
        fillTableName(depDataSet);
        data.setDepDataSet(depDataSet);
        //入参准备
        VirtualArgs args = callback.prepareArgs(depDataSet);
        data.setArgs(args);
        // 预期的db数据准备
        VirtualDataSet expectDataSet = callback.prepareExpectDataSet(args, depDataSet);
        fillTableName(expectDataSet);
        data.setExpectDataSet(expectDataSet);
        // 预期结果准备
        Object expectResultObj = callback.prepareExpectResult(args, depDataSet);
        VirtualResult result = new VirtualResult(expectResultObj);
        data.setExpectResult(result);
        // mock准备
//        VirtualMockSet virtualMockSet = callback.prepareMockResult();
        //校验的事件准备
        if (callback instanceof PrepareCallBackWithEvent) {
            VirtualEventSet virtualEventSet = ((PrepareCallBackWithEvent) callback)
                    .prepareExpectEventSet(args, depDataSet, expectResultObj);
            data.setExpectEventSet(virtualEventSet);
        }

        return data;
    }

    /**
     * 将对象属性转化为列名
     *
     * @param fieldName
     * @return
     */
    protected String convertFieldName2ColumnName(String fieldName) {

        if (fieldName.contains("_")) {
            return fieldName;
        }
        String[] strs = StringUtils.splitByCharacterTypeCamelCase(fieldName);
        for (int i = 0; i < strs.length; i++) {
            strs[i] = strs[i];
        }
        String fieldColumnName = StringUtils.join(strs, "_");
        return fieldColumnName;

    }

    /**
     * 通过DAO获取tableName填入VirtualTable
     *
     * @param dataSet
     */
    private void fillTableName(VirtualDataSet dataSet) {
        if (dataSet == null) {
            return;
        }
        for (VirtualTable virtualTable : dataSet.getVirtualTables()) {

            if (virtualTable.getTableName() == null) {
                Map<String, String> flagMap = virtualTable.getFlags();
                if (flagMap != null) {
                    List<String> keyList = new ArrayList<String>();
                    for (String flagKey : flagMap.keySet()) {
                        keyList.add(flagKey);

                    }
                    for (String key : keyList) {
                        String tmpFlag = flagMap.get(key);
                        flagMap.remove(key);
                        flagMap.put(convertFieldName2ColumnName(key), tmpFlag);

                    }

                }

                List<Map<String, Object>> tableData = new ArrayList<Map<String, Object>>();
                for (Map<String, Object> row : virtualTable.getTableData()) {
                    List<String> keyList = new ArrayList<String>();
                    for (String rowKey : row.keySet()) {
                        keyList.add(rowKey);
                    }
                    for (String key : keyList) {
                        Object tmpData = row.get(key);
                        row.remove(key);
                        row.put(convertFieldName2ColumnName(key), tmpData);

                    }
                    tableData.add(row);
                }
                virtualTable.setTableData(tableData);
                virtualTable.setFlags(flagMap);
                DataAccessConfig dataAccessConfig = dataAccessConfigManager
                        .findDataAccessConfig(virtualTable.getDataObjClazz());
                if (dataAccessConfig != null) {
                    virtualTable.setTableName(dataAccessConfig.getTableName());
                }
            }
        }
    }

    public DataAccessConfigManager getDataAccessConfigManager() {
        return dataAccessConfigManager;
    }

    public void setDataAccessConfigManager(DataAccessConfigManager dataAccessConfigManager) {
        this.dataAccessConfigManager = dataAccessConfigManager;
    }
}
