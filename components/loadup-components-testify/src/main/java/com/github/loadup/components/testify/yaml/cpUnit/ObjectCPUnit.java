/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import com.github.loadup.components.testify.constant.TestifyPathConstants;
import com.github.loadup.components.testify.yaml.cpUnit.property.BaseUnitProperty;
import com.github.loadup.components.testify.yaml.cpUnit.property.ListObjectUnitProperty;
import com.github.loadup.components.testify.yaml.cpUnit.property.ObjectUnitProperty;
import com.github.loadup.components.testify.yaml.enums.CPUnitTypeEnum;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 对象CheckPoint单位
 *
 *
 *
 */
public class ObjectCPUnit extends BaseCPUnit {

    /**
     * 映射对应ObjectUnitProperty通用属性
     */
    private final ObjectUnitProperty property;

    /**
     * 映射对应ObjectUnitProperty通用属性Map
     */
    private final Map<String, BaseUnitProperty> attributeMap;

    public ObjectCPUnit(String unitName, Map<String, Object> rawData) {
        this.unitName = unitName;
        this.description = "" + rawData.get("__desc");
        this.unitType = CPUnitTypeEnum.OBJECT;
        this.targetCSVPath = TestifyPathConstants.OBJECT_DATA_PATH + this.unitName + "/" + this.unitName + ".csv";
        for (Entry<String, Object> entry : rawData.entrySet()) {
            String keyName = entry.getKey();
            if (keyName.startsWith("$")) {
                this.specialMap.put(keyName, entry.getValue());
                rawData.remove(keyName);
            }
        }
        this.property = new ObjectUnitProperty(this.unitName, this.unitName, this.targetCSVPath, rawData);
        this.attributeMap = this.property.getAttributeMap();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "ObjectCPUnit [unitType="
                + unitType
                + ", attributeMap="
                + attributeMap
                + ", unitName="
                + unitName
                + ", description="
                + description
                + ", targetCSVPath="
                + targetCSVPath
                + ", specialMap="
                + specialMap
                + "]";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object dump() {
        Map dumpMap = new LinkedHashMap();
        dumpMap.put("__desc", this.description);
        for (Entry<String, BaseUnitProperty> entry : this.attributeMap.entrySet()) {
            BaseUnitProperty property = entry.getValue();
            if (property instanceof ObjectUnitProperty) {
                Map objMap = (Map) ((ObjectUnitProperty) entry.getValue()).dump(entry.getKey());
                dumpMap.putAll(objMap);
            } else if (property instanceof ListObjectUnitProperty) {
                Map objMap = (Map) ((ListObjectUnitProperty) entry.getValue()).dump(entry.getKey());
                dumpMap.putAll(objMap);
            } else {
                dumpMap.putAll((Map) entry.getValue().dump());
            }
        }
        return dumpMap;
    }

    /**
     *
     *
     * @return property value of attributeMap
     */
    public Map<String, BaseUnitProperty> getAttributeMap() {
        return attributeMap;
    }

    /**
     *
     *
     * @return property value of property
     */
    public ObjectUnitProperty getProperty() {
        return property;
    }
}
