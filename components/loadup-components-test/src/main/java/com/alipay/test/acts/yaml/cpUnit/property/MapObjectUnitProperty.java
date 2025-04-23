/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.yaml.cpUnit.property;

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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 列表对象基础属性
 * 
 * @author baishuo.lp
 * @version $Id: ListObjectUnitProperty.java, v 0.1 2015年8月12日 下午4:40:26 baishuo.lp Exp $
 */
public class MapObjectUnitProperty extends BaseUnitProperty {
    private static final Log                    logger    = LogFactory
                                                              .getLog(MapObjectUnitProperty.class);

    private String                              targetCSVPath;

    private Class<?>                            classType;

    /** 对象属性列表*/
    private final Map<String, BaseUnitProperty> objectMap = new LinkedHashMap<String, BaseUnitProperty>();

    @SuppressWarnings("unchecked")
    public MapObjectUnitProperty(String keyName, String keyPath, String parentCSVPath,
                                 Map<String, BaseUnitProperty> value) {
        super(keyName, keyPath, null);
        if (value != null) {
            for (String key : value.keySet()) {

                String currentKeyPath = this.keyPath + "." + key;
                BaseUnitProperty property = value.get(key);

                this.objectMap.put(key, property);
            }

        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object genObject(ClassLoader classLoader) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (String key : this.objectMap.keySet()) {
            BaseUnitProperty property = this.objectMap.get(key);
            if (property instanceof ObjectUnitProperty) {
                ObjectUnitProperty childUnit = (ObjectUnitProperty) property;
                childUnit.setClassType(this.classType);
                map.put(key, childUnit.genObject(classLoader));
            } else {
                map.put(key, property.getExpectValue());
            }
        }
        return map;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "MapObjectUnitProperty [objectMap=" + objectMap + ", keyName=" + keyName
               + ", flagCode=" + flagCode + ", keyPath=" + keyPath + "]";
    }

    /**
     * Getter method for property <tt>objectList</tt>.
     * 
     * @return property value of objectList
     */
    public Map<String, BaseUnitProperty> getObjectMap() {
        return objectMap;
    }

    /**
     * Getter method for property <tt>targetCSVPath</tt>.
     * 
     * @return property value of targetCSVPath
     */
    public String getTargetCSVPath() {
        return targetCSVPath;
    }

    /**
     * Setter method for property <tt>targetCSVPath</tt>.
     * 
     * @param targetCSVPath value to be assigned to property targetCSVPath
     */
    public void setTargetCSVPath(String targetCSVPath) {
        this.targetCSVPath = targetCSVPath;
    }

    /**
     * Getter method for property <tt>classType</tt>.
     * 
     * @return property value of classType
     */
    public Class<?> getClassType() {
        return classType;
    }

    /**
     * Setter method for property <tt>classType</tt>.
     * 
     * @param classType value to be assigned to property classType
     */
    public void setClassType(Class<?> classType) {
        this.classType = classType;
    }

}
