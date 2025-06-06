/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.context;

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

import com.github.loadup.components.testify.driver.enums.SuiteFlag;
import com.github.loadup.components.testify.yaml.YamlTestData;
import com.github.loadup.components.testify.yaml.cpUnit.DataBaseCPUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用例上下文
 *
 *
 *
 */
public class TestifyCaseContext {

    // 用例当前CP点替换字段
    private final Map<String, Object> uniqueMap = new HashMap<String, Object>();
    // 过程日志信息
    private final List<String> logData = new ArrayList<String>();
    // 数据待清除区域行对象
    private final List<DataBaseCPUnit> preCleanContent = new ArrayList<DataBaseCPUnit>();
    // CP点当前错误列表
    private final List<String> processErrorLog = new ArrayList<String>();
    // 用例caseID
    private String caseId;
    // 用例描述
    private String caseDesc;
    // 用例执行标识
    private SuiteFlag suiteFlag;
    // 用例当前入参
    private Map<String, Object> parameterMap;
    // 是否已加载公共准备数据字段
    private boolean needLoadCommonSection = false;

    // 需要比较数据库表长度
    private boolean needCompareTableLength = true;

    // 当前用例驱动yaml文件名，可以为空
    private String yamlPath;

    // Yaml加载对象
    private YamlTestData yamlTestData;

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
     * @return property value of parameterMap
     */
    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    /**
     *
     *
     * @param parameterMap value to be assigned to property parameterMap
     */
    public void setParameterMap(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
    }

    /**
     *
     *
     * @return property value of needLoadCommonSection
     */
    public boolean isNeedLoadCommonSection() {
        return needLoadCommonSection;
    }

    /**
     *
     *
     * @param needLoadCommonSection value to be assigned to property needLoadCommonSection
     */
    public void setNeedLoadCommonSection(boolean needLoadCommonSection) {
        this.needLoadCommonSection = needLoadCommonSection;
    }

    /**
     *
     *
     * @return property value of logData
     */
    public List<String> getLogData() {
        return logData;
    }

    /**
     *
     *
     * @return property value of preCleanContent
     */
    public List<DataBaseCPUnit> getPreCleanContent() {
        return preCleanContent;
    }

    /**
     *
     *
     * @return property value of caseDesc
     */
    public String getCaseDesc() {
        return caseDesc;
    }

    /**
     *
     *
     * @param caseDesc value to be assigned to property caseDesc
     */
    public void setCaseDesc(String caseDesc) {
        this.caseDesc = caseDesc;
    }

    /**
     *
     *
     * @return property value of suiteFlag
     */
    public SuiteFlag getSuiteFlag() {
        return suiteFlag;
    }

    /**
     *
     *
     * @param suiteFlag value to be assigned to property suiteFlag
     */
    public void setSuiteFlag(SuiteFlag suiteFlag) {
        this.suiteFlag = suiteFlag;
    }

    /**
     *
     *
     * @return property value of uniqueMap
     */
    public Map<String, Object> getUniqueMap() {
        return uniqueMap;
    }

    /**
     *
     *
     * @return property value of processErrorLog
     */
    public List<String> getProcessErrorLog() {
        return processErrorLog;
    }

    /**
     *
     *
     * @return property value of needCompareTableLength
     */
    public boolean isNeedCompareTableLength() {
        return needCompareTableLength;
    }

    /**
     *
     *
     * @param needCompareTableLength value to be assigned to property needCompareTableLength
     */
    public void setNeedCompareTableLength(boolean needCompareTableLength) {
        this.needCompareTableLength = needCompareTableLength;
    }

    /**
     *
     *
     * @return property value of yamlTestData
     */
    public YamlTestData getYamlTestData() {
        return yamlTestData;
    }

    /**
     *
     *
     * @param yamlTestData value to be assigned to property yamlTestData
     */
    public void setYamlTestData(YamlTestData yamlTestData) {
        this.yamlTestData = yamlTestData;
    }

    /**
     *
     *
     * @return property value of yamlPath
     */
    public String getYamlPath() {
        return yamlPath;
    }

    /**
     *
     *
     * @param yamlPath value to be assigned to property yamlPath
     */
    public void setYamlPath(String yamlPath) {
        this.yamlPath = yamlPath;
    }
}
