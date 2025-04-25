/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.yaml;

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
import java.util.Map.Entry;

import com.github.loadup.components.testify.constant.TestifyYamlConstants;

/**
 * 
 *
 */
public class YamlTestCase {

    /**
     * CP列表
     */
    private final Map<String, YamlCheckPoint> checkPointMap = new LinkedHashMap<String, YamlCheckPoint>();
    /**
     * 用例ID
     */
    private String caseId;
    /**
     * 用例描述
     */
    private String description;

    //~~~ 构造方法
    @SuppressWarnings("unchecked")
    public YamlTestCase(String caseId, Map<String, Object> testCaseData) {
        this.caseId = caseId;
        this.description = (String) testCaseData.get("__desc");
        testCaseData.remove("__desc");

        if (this.caseId.equals(TestifyYamlConstants.COMMONKEY)) {
            //加载通用区域
            YamlCheckPoint checkPoint = new YamlCheckPoint(TestifyYamlConstants.COMMONKEY,
                    testCaseData);
            this.checkPointMap.put(TestifyYamlConstants.COMMONKEY, checkPoint);
        } else {
            //加载用例区域
            for (Entry<String, Object> entry : testCaseData.entrySet()) {
                String checkPointName = entry.getKey();
                Map<String, Object> checkPointData = (Map<String, Object>) entry.getValue();
                YamlCheckPoint checkPoint = new YamlCheckPoint(checkPointName, checkPointData);
                this.checkPointMap.put(checkPointName, checkPoint);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map dump() {
        Map dumpMap = new LinkedHashMap();
        if (this.caseId.equals(TestifyYamlConstants.COMMONKEY)) {
            dumpMap.putAll(this.checkPointMap.get(TestifyYamlConstants.COMMONKEY).dump());
        } else {
            for (Entry<String, YamlCheckPoint> entry : this.checkPointMap.entrySet()) {
                String unitName = entry.getKey();
                YamlCheckPoint cpUnit = entry.getValue();
                dumpMap.put(unitName, cpUnit.dump());
            }
        }
        return dumpMap;
    }

    //~~~ 公用方法

    public YamlCheckPoint getCheckPoint(String checkPointName) {
        return checkPointMap.get(checkPointName);
    }

    //~~~ 容器方法

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "YamlTestCase [caseId=" + caseId + ", description=" + description
                + ", checkPointMap=" + checkPointMap + "]";
    }

    /**
     *
     *
     * @return property value of caseId
     */
    public String getCaseId() {
        return caseId;
    }

    /**
     * 
     *
     * @param caseId value to be assigned to property caseId
     */
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    /**
     *
     *
     * @return property value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * 
     *
     * @param description value to be assigned to property description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     *
     * @return property value of checkPointList
     */
    public Map<String, YamlCheckPoint> getCheckPointMap() {
        return checkPointMap;
    }

}
