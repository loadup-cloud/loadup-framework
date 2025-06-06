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

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 列表对象基础属性
 *
 *
 *
 */
public class MapObjectUnitProperty extends BaseUnitProperty {
    private static final Logger log = LoggerFactory.getLogger(MapObjectUnitProperty.class);

    /**
     * 对象属性列表
     */
    private final Map<String, BaseUnitProperty> objectMap = new LinkedHashMap<String, BaseUnitProperty>();

    private String targetCSVPath;
    private Class<?> classType;

    @SuppressWarnings("unchecked")
    public MapObjectUnitProperty(
            String keyName, String keyPath, String parentCSVPath, Map<String, BaseUnitProperty> value) {
        super(keyName, keyPath, null);
        if (value != null) {
            for (String key : value.keySet()) {

                String currentKeyPath = this.keyPath + "." + key;
                BaseUnitProperty property = value.get(key);

                this.objectMap.put(key, property);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
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
        return "MapObjectUnitProperty [objectMap="
                + objectMap
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
    public Map<String, BaseUnitProperty> getObjectMap() {
        return objectMap;
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
