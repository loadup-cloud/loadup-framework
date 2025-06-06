/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.yaml.cpUnit.property;

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

import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.object.TestifyObjectUtil;
import com.github.loadup.components.testify.yaml.cpUnit.ListObjectCPUnit;
import com.github.loadup.components.testify.yaml.cpUnit.ObjectCPUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 列表对象基础属性
 *
 *
 *
 */
@Slf4j
public class ListObjectUnitProperty extends BaseUnitProperty {

    private String targetCSVPath;

    private Class<?> classType;

    /**
     * 对象属性列表
     */
    private List<BaseUnitProperty> objectList = new ArrayList<BaseUnitProperty>();

    @SuppressWarnings("unchecked")
    public ListObjectUnitProperty(String keyName, String keyPath, String parentCSVPath, List<Object> value) {
        super(keyName, keyPath, null);
        for (int i = 0; i < value.size(); i++) {
            Object obj = value.get(i);
            String currentKeyPath = this.keyPath + "." + i;
            BaseUnitProperty property;
            if (obj instanceof Map) {
                Map<String, Object> mapObj = (Map<String, Object>) obj;
                if (mapObj.get("__desc") != null) {
                    // 按复杂类处理
                    property = new ObjectUnitProperty(keyName, currentKeyPath, parentCSVPath, mapObj);
                } else {
                    // 按普通Map对象处理
                    property = new BaseUnitProperty(keyName, currentKeyPath, value);
                }
            } else if (obj instanceof List) {
                List<Object> listObj = (List<Object>) obj;
                property = new ListObjectUnitProperty(keyName, currentKeyPath, parentCSVPath, listObj);
            } else {
                property = new BaseUnitProperty(keyName, currentKeyPath, value);
            }
            this.objectList.add(property);
        }
    }

    /**
     * 内部构造方法，在对象生成时用于递归
     *
     * @param keyName
     * @param keyPath
     * @param targetCSVPath
     * @param objList
     */
    public ListObjectUnitProperty(ListObjectCPUnit unit) {
        super(unit.getUnitName(), unit.getUnitName(), null);
        //        Assert.assertTrue("列表对象集合数字必须大于0", unit.getAttributeList().size() > 0);
        List<BaseUnitProperty> objectList = new ArrayList<BaseUnitProperty>();
        for (ObjectCPUnit objUnit : unit.getAttributeList()) {
            ObjectUnitProperty property = objUnit.getProperty();
            objectList.add(property);
        }
        this.targetCSVPath = unit.getTargetCSVPath();
        this.objectList = objectList;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object genObject(ClassLoader classLoader) {
        List list = new ArrayList();
        for (BaseUnitProperty property : this.getObjectList()) {
            //            Assert.assertTrue("本方法仅支持复杂List对象调用", property instanceof
            // ObjectUnitProperty);
            ObjectUnitProperty childUnit = (ObjectUnitProperty) property;
            childUnit.setClassType(this.classType);
            list.add(childUnit.genObject(classLoader));
        }
        return list;
    }

    /**
     * 对象列表比较工具类，按序校验
     *
     * @param object
     * @param unit
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void compare(Object object) {
        List actualList = (List) object;
        if (actualList.size() != this.objectList.size()) {
            TestifyLogUtil.error(
                    log, this.keyName + "列对象长度不同，期望值:" + this.objectList.size() + "，实际值:" + actualList.size());
        }
        for (int i = 0; i < actualList.size(); i++) {
            ObjectUnitProperty childProperty = (ObjectUnitProperty) this.objectList.get(i);
            childProperty.compare(actualList.get(i));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object dump(String fieldName) {
        Map dumpMap = new LinkedHashMap();
        List objlist = new ArrayList();
        boolean isComplexList = false;
        boolean needDump = false;
        String expect = "";
        for (BaseUnitProperty property : this.objectList) {
            // 注意，dump不允许list出现嵌套List模式
            if (property instanceof ObjectUnitProperty) {
                isComplexList = true;
                ObjectUnitProperty objProperty = (ObjectUnitProperty) property;
                Map obj = (Map) objProperty.dump(fieldName);
                objlist.add(obj.get(fieldName));
                if (obj.size() != 0) {
                    needDump = true;
                }
            } else {
                Object obj = ((Map) property.dump()).get(fieldName);
                objlist.add(obj);
                expect += (";" + obj);
            }
        }
        if (!isComplexList) {
            needDump = !TestifyObjectUtil.easyCompare(expect.substring(1), this.baseValue);
        }

        if (needDump && isComplexList) {
            dumpMap.put(fieldName, objlist);
        } else if (needDump) {
            dumpMap.put(fieldName, expect.substring(1));
        }
        return dumpMap;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "ListObjectUnitProperty [objectList="
                + objectList
                + ", keyName="
                + keyName
                + ", flagCode="
                + flagCode
                + ", keyPath="
                + keyPath
                + "]";
    }

    /**
     *
     *
     * @return property value of objectList
     */
    public List<BaseUnitProperty> getObjectList() {
        return objectList;
    }

    /**
     *
     *
     * @return property value of targetCSVPath
     */
    public String getTargetCSVPath() {
        return targetCSVPath;
    }

    /**
     *
     *
     * @param targetCSVPath value to be assigned to property targetCSVPath
     */
    public void setTargetCSVPath(String targetCSVPath) {
        this.targetCSVPath = targetCSVPath;
    }

    /**
     *
     *
     * @return property value of classType
     */
    public Class<?> getClassType() {
        return classType;
    }

    /**
     *
     *
     * @param classType value to be assigned to property classType
     */
    public void setClassType(Class<?> classType) {
        this.classType = classType;
    }
}
