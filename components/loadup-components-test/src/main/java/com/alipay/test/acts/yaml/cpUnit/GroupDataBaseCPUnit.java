/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.yaml.cpUnit;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.constant.ActsPathConstants;
import com.alipay.test.acts.yaml.enums.CPUnitTypeEnum;

/**
 * 组数据CheckPoint单位
 * 
 * @author baishuo.lp
 * @version $Id: GroupDataBaseCPUnit.java, v 0.1 2015年8月12日 上午11:23:52 baishuo.lp Exp $
 */
public class GroupDataBaseCPUnit extends BaseCPUnit {

    /** 用做select组数据的列名数组*/
    private final String[]             conditionKeys;

    private final String               orderBy;

    private final List<DataBaseCPUnit> dataList = new ArrayList<DataBaseCPUnit>();

    @SuppressWarnings("unchecked")
    public GroupDataBaseCPUnit(String unitName, Map<String, Object> rawData) {
        this.unitName = unitName.substring(6);
        this.description = "" + rawData.get("__desc");
        rawData.remove("__desc");
        this.unitType = CPUnitTypeEnum.GROUP;
        this.targetCSVPath = ActsPathConstants.DB_DATA_PATH + this.unitName + ".csv";
        this.conditionKeys = ((String) rawData.get("__conditionKeys")).split(",");
        this.orderBy = (String) rawData.get("__orderBy");
        String tableName = unitName.substring(6);
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) rawData.get(tableName);

        for (Map<String, Object> data : dataList) {
            DataBaseCPUnit dataBaseUnit = new DataBaseCPUnit(tableName, data);
            this.dataList.add(dataBaseUnit);
        }
    }

    /**
     * Getter method for property <tt>conditionKeys</tt>.
     * 
     * @return property value of conditionKeys
     */
    public String[] getConditionKeys() {
        return conditionKeys;
    }

    /**
     * Getter method for property <tt>dataList</tt>.
     * 
     * @return property value of dataList
     */
    public List<DataBaseCPUnit> getDataList() {
        return dataList;
    }

    /**
     * Getter method for property <tt>orderBy</tt>.
     * 
     * @return property value of orderBy
     */
    public String getOrderBy() {
        return orderBy;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "GroupDataBaseCPUnit [unitType=" + unitType + ", conditionKeys="
               + Arrays.toString(conditionKeys) + ", orderBy=" + orderBy + ", dataList=" + dataList
               + ", unitName=" + unitName + "]";
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object dump() {
        Map dumpMap = new TreeMap();
        dumpMap.put("__conditionKeys", StringUtils.join(this.conditionKeys, ","));
        dumpMap.put("__orderBy", this.orderBy);
        List listData = new ArrayList();
        for (DataBaseCPUnit dbUnit : this.dataList) {
            listData.add(dbUnit.dump());
        }
        dumpMap.put(this.unitName, listData);
        return dumpMap;
    }
}
