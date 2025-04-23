/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.yaml;

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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.Assert;

import com.alipay.test.acts.cache.ActsCacheData;
import com.alipay.test.acts.yaml.cpUnit.BaseCPUnit;
import com.alipay.test.acts.yaml.cpUnit.DataBaseCPUnit;
import com.alipay.test.acts.yaml.cpUnit.GroupDataBaseCPUnit;
import com.alipay.test.acts.yaml.cpUnit.ListDataBaseCPUnit;
import com.alipay.test.acts.yaml.cpUnit.ListObjectCPUnit;
import com.alipay.test.acts.yaml.cpUnit.MessageCPUnit;
import com.alipay.test.acts.yaml.cpUnit.ObjectCPUnit;
import com.alipay.test.acts.yaml.enums.CPUnitTypeEnum;

/**
 * CP点模型
 * 
 * @author baishuo.lp
 * @version $Id: YamlCheckPoint.java, v 0.1 2015年8月12日 上午11:09:37 baishuo.lp Exp $
 */
public class YamlCheckPoint {

    /** CP点名称, 建议直接写为描述形式*/
    private String                        checkPointName;

    /** CP点数据Map*/
    private final Map<String, BaseCPUnit> checkPointUnitMap = new LinkedHashMap<String, BaseCPUnit>();

    // ~~~ 构造方法
    @SuppressWarnings("unchecked")
    public YamlCheckPoint(String checkPointName, Map<String, Object> checkPointData) {
        this.checkPointName = checkPointName;
        for (Entry<String, Object> entry : checkPointData.entrySet()) {
            String unitName = entry.getKey();
            CPUnitTypeEnum unitType = ActsCacheData.getCPUnitType(unitName);
            BaseCPUnit unit = null;
            if (unitType == null) {
                Assert.fail(unitName + "校验点字段不存在，请手动添加");
            }
            if (entry.getValue() != null) {
                switch (unitType) {
                    case DATABASE:
                        Object value = entry.getValue();
                        if (value instanceof Map) {
                            unit = new DataBaseCPUnit(unitName,
                                (Map<String, Object>) entry.getValue());
                        } else if (value instanceof List) {
                            unit = new ListDataBaseCPUnit(unitName, (List<Object>) value);
                        } else {
                            Assert.fail(unitName + "cp点格式不正确");
                        }
                        break;
                    case GROUP:
                        unit = new GroupDataBaseCPUnit(unitName,
                            (Map<String, Object>) entry.getValue());
                        break;
                    case OBJECT:
                        if (entry.getValue() instanceof Map) {
                            unit = new ObjectCPUnit(unitName,
                                (Map<String, Object>) entry.getValue());
                        } else if (entry.getValue() instanceof List) {
                            unit = new ListObjectCPUnit(unitName,
                                (List<Map<String, Object>>) entry.getValue());
                        } else if (entry.getValue() != null) {
                            Assert.fail("准备对象格式未知异常");
                        }
                        break;
                    case MESSAGE:
                        unit = new MessageCPUnit(unitName, (List<Object>) entry.getValue());
                        break;
                }
            }
            this.checkPointUnitMap.put(unitName, unit);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map dump() {
        Map dumpMap = new LinkedHashMap();
        for (Entry<String, BaseCPUnit> entry : this.checkPointUnitMap.entrySet()) {
            String unitName = entry.getKey();
            BaseCPUnit cpUnit = entry.getValue();
            dumpMap.put(unitName, cpUnit.dump());
        }
        return dumpMap;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "YamlCheckPoint [checkPointName=" + checkPointName + ", checkPointUnitMap="
               + checkPointUnitMap + "]";
    }

    // ~~~ 容器方法

    /**
     * Getter method for property <tt>checkPointName</tt>.
     * 
     * @return property value of checkPointName
     */
    public String getCheckPointName() {
        return checkPointName;
    }

    /**
     * Setter method for property <tt>checkPointName</tt>.
     * 
     * @param checkPointName value to be assigned to property checkPointName
     */
    public void setCheckPointName(String checkPointName) {
        this.checkPointName = checkPointName;
    }

    /**
     * Getter method for property <tt>checkPointUnitMap</tt>.
     * 
     * @return property value of checkPointUnitMap
     */
    public Map<String, BaseCPUnit> getCheckPointUnitMap() {
        return checkPointUnitMap;
    }

}
