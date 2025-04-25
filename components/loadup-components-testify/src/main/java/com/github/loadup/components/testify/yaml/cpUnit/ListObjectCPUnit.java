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
 * 对象CheckPoint单位
 *
 * 
 * 
 */
public class ListObjectCPUnit extends BaseCPUnit {

    private List<ObjectCPUnit> attributeList = new ArrayList<ObjectCPUnit>();

    /**
     * 默认Yaml文件类型初始化时构造函数
     *
     * @param unitName
     * @param rawData
     */
    public ListObjectCPUnit(String unitName, List<Map<String, Object>> rawData) {
        this.unitName = unitName;
        this.unitType = CPUnitTypeEnum.OBJECT;
        for (Map<String, Object> data : rawData) {
            ObjectCPUnit objectCPUnit = new ObjectCPUnit(unitName, data);
            this.attributeList.add(objectCPUnit);
        }
    }

    /**
     * 消息对象转换时构造函数
     *
     * @param attributeList
     */
    public ListObjectCPUnit(MessageCPUnit msgCPUnit) {
        this.unitName = msgCPUnit.getUnitName();
        this.unitType = CPUnitTypeEnum.OBJECT;
        this.attributeList = msgCPUnit.getAttributeList();
        this.targetCSVPath = msgCPUnit.getTargetCSVPath();
    }

    /**
     * 
     *
     * @return property value of attributeList
     */
    public List<ObjectCPUnit> getAttributeList() {
        return attributeList;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "ListObjectCPUnit [unitType=" + unitType + ", attributeList=" + attributeList
                + ", unitName=" + unitName + "]";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object dump() {
        List objList = new ArrayList();
        for (ObjectCPUnit cpUnit : this.attributeList) {
            objList.add(cpUnit.dump());
        }
        return objList;
    }
}
