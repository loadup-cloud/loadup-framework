/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.yaml.cpUnit;

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
import java.util.List;
import java.util.Map;

import com.github.loadup.components.testify.yaml.enums.CPUnitTypeEnum;

/**
 * 组数据CheckPoint单位
 *
 * 
 *
 */
public class ListDataBaseCPUnit extends BaseCPUnit {

    private final List<DataBaseCPUnit> dataList = new ArrayList<DataBaseCPUnit>();

    @SuppressWarnings("unchecked")
    public ListDataBaseCPUnit(String unitName, List<Object> rawData) {
        this.unitName = unitName;
        this.unitType = CPUnitTypeEnum.DATABASE;

        for (Object data : rawData) {
            DataBaseCPUnit dataBaseUnit = new DataBaseCPUnit(unitName, (Map<String, Object>) data);
            this.dataList.add(dataBaseUnit);
        }
    }

    /**
     * 
     *
     * @return property value of dataList
     */
    public List<DataBaseCPUnit> getDataList() {
        return dataList;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "ListDataBaseCPUnit [dataList=" + dataList + ", unitName=" + unitName
                + ", unitType=" + unitType + "]";
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object dump() {
        List listData = new ArrayList();
        for (DataBaseCPUnit dbUnit : this.dataList) {
            listData.add(dbUnit.dump());
        }
        return listData;
    }
}
