package com.alipay.test.acts.context;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alipay.test.acts.driver.enums.SuiteFlag;
import com.alipay.test.acts.yaml.YamlTestData;
import com.alipay.test.acts.yaml.cpUnit.DataBaseCPUnit;

/**
 * Acts用例上下文
 * 
 * @author baishuo.lp
 * @version $Id: ActsCaseContext.java, v 0.1 2015年8月16日 下午11:04:53 baishuo.lp Exp $
 */
public class ActsCaseContext {

    //用例caseID
    private String                     caseId;

    //用例描述
    private String                     caseDesc;

    //用例执行标识
    private SuiteFlag                  suiteFlag;

    //用例当前入参
    private Map<String, Object>        parameterMap;

    //用例当前CP点替换字段
    private final Map<String, Object>  uniqueMap              = new HashMap<String, Object>();

    //过程日志信息
    private final List<String>         logData                = new ArrayList<String>();

    //数据待清除区域行对象
    private final List<DataBaseCPUnit> preCleanContent        = new ArrayList<DataBaseCPUnit>();

    //CP点当前错误列表
    private final List<String>         processErrorLog        = new ArrayList<String>();

    //是否已加载公共准备数据字段
    private boolean                    needLoadCommonSection  = false;

    //需要比较数据库表长度
    private boolean                    needCompareTableLength = true;

    //当前用例驱动yaml文件名，可以为空
    private String                     yamlPath;

    //Yaml加载对象
    private YamlTestData               yamlTestData;

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
     * Getter method for property <tt>parameterMap</tt>.
     * 
     * @return property value of parameterMap
     */
    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    /**
     * Setter method for property <tt>parameterMap</tt>.
     * 
     * @param parameterMap value to be assigned to property parameterMap
     */
    public void setParameterMap(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
    }

    /**
     * Getter method for property <tt>needLoadCommonSection</tt>.
     * 
     * @return property value of needLoadCommonSection
     */
    public boolean isNeedLoadCommonSection() {
        return needLoadCommonSection;
    }

    /**
     * Setter method for property <tt>needLoadCommonSection</tt>.
     * 
     * @param needLoadCommonSection value to be assigned to property needLoadCommonSection
     */
    public void setNeedLoadCommonSection(boolean needLoadCommonSection) {
        this.needLoadCommonSection = needLoadCommonSection;
    }

    /**
     * Getter method for property <tt>logData</tt>.
     * 
     * @return property value of logData
     */
    public List<String> getLogData() {
        return logData;
    }

    /**
     * Getter method for property <tt>preCleanContent</tt>.
     * 
     * @return property value of preCleanContent
     */
    public List<DataBaseCPUnit> getPreCleanContent() {
        return preCleanContent;
    }

    /**
     * Getter method for property <tt>caseDesc</tt>.
     * 
     * @return property value of caseDesc
     */
    public String getCaseDesc() {
        return caseDesc;
    }

    /**
     * Setter method for property <tt>caseDesc</tt>.
     * 
     * @param caseDesc value to be assigned to property caseDesc
     */
    public void setCaseDesc(String caseDesc) {
        this.caseDesc = caseDesc;
    }

    /**
     * Getter method for property <tt>suiteFlag</tt>.
     * 
     * @return property value of suiteFlag
     */
    public SuiteFlag getSuiteFlag() {
        return suiteFlag;
    }

    /**
     * Setter method for property <tt>suiteFlag</tt>.
     * 
     * @param suiteFlag value to be assigned to property suiteFlag
     */
    public void setSuiteFlag(SuiteFlag suiteFlag) {
        this.suiteFlag = suiteFlag;
    }

    /**
     * Getter method for property <tt>uniqueMap</tt>.
     * 
     * @return property value of uniqueMap
     */
    public Map<String, Object> getUniqueMap() {
        return uniqueMap;
    }

    /**
     * Getter method for property <tt>processErrorLog</tt>.
     * 
     * @return property value of processErrorLog
     */
    public List<String> getProcessErrorLog() {
        return processErrorLog;
    }

    /**
     * Getter method for property <tt>needCompareTableLength</tt>.
     * 
     * @return property value of needCompareTableLength
     */
    public boolean isNeedCompareTableLength() {
        return needCompareTableLength;
    }

    /**
     * Setter method for property <tt>needCompareTableLength</tt>.
     * 
     * @param needCompareTableLength value to be assigned to property needCompareTableLength
     */
    public void setNeedCompareTableLength(boolean needCompareTableLength) {
        this.needCompareTableLength = needCompareTableLength;
    }

    /**
     * Getter method for property <tt>yamlTestData</tt>.
     * 
     * @return property value of yamlTestData
     */
    public YamlTestData getYamlTestData() {
        return yamlTestData;
    }

    /**
     * Setter method for property <tt>yamlTestData</tt>.
     * 
     * @param yamlTestData value to be assigned to property yamlTestData
     */
    public void setYamlTestData(YamlTestData yamlTestData) {
        this.yamlTestData = yamlTestData;
    }

    /**
     * Getter method for property <tt>yamlPath</tt>.
     * 
     * @return property value of yamlPath
     */
    public String getYamlPath() {
        return yamlPath;
    }

    /**
     * Setter method for property <tt>yamlPath</tt>.
     * 
     * @param yamlPath value to be assigned to property yamlPath
     */
    public void setYamlPath(String yamlPath) {
        this.yamlPath = yamlPath;
    }

}
