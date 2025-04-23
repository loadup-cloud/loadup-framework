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
import java.util.Map;
import java.util.Map.Entry;

import com.alipay.test.acts.constant.ActsYamlConstants;

/**
 * 
 * @author baishuo.lp
 * @version $Id: YamlTestCase.java, v 0.1 2015年8月12日 上午11:06:27 baishuo.lp Exp $
 */
public class YamlTestCase {

    /** 用例ID*/
    private String                            caseId;

    /** 用例描述*/
    private String                            description;

    /** CP列表*/
    private final Map<String, YamlCheckPoint> checkPointMap = new LinkedHashMap<String, YamlCheckPoint>();

    //~~~ 构造方法
    @SuppressWarnings("unchecked")
    public YamlTestCase(String caseId, Map<String, Object> testCaseData) {
        this.caseId = caseId;
        this.description = (String) testCaseData.get("__desc");
        testCaseData.remove("__desc");

        if (this.caseId.equals(ActsYamlConstants.COMMONKEY)) {
            //加载通用区域
            YamlCheckPoint checkPoint = new YamlCheckPoint(ActsYamlConstants.COMMONKEY,
                testCaseData);
            this.checkPointMap.put(ActsYamlConstants.COMMONKEY, checkPoint);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map dump() {
        Map dumpMap = new LinkedHashMap();
        if (this.caseId.equals(ActsYamlConstants.COMMONKEY)) {
            dumpMap.putAll(this.checkPointMap.get(ActsYamlConstants.COMMONKEY).dump());
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
     * Getter method for property <tt>caseId</tt>.
     * 
     * @return property value of caseId
     */
    public String getCaseId() {
        return caseId;
    }

    /**
     * Setter method for property <tt>caseId</tt>.
     * 
     * @param caseId value to be assigned to property caseId
     */
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    /**
     * Getter method for property <tt>description</tt>.
     * 
     * @return property value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter method for property <tt>description</tt>.
     * 
     * @param description value to be assigned to property description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter method for property <tt>checkPointList</tt>.
     * 
     * @return property value of checkPointList
     */
    public Map<String, YamlCheckPoint> getCheckPointMap() {
        return checkPointMap;
    }

}
