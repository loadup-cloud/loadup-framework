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

import com.github.loadup.components.testify.yaml.YamlTestData;
import java.util.List;

/**
 * 脚本上下文
 *
 *
 *
 */
public class TestifySuiteContext {

    // 用例当前类名
    private String className;

    // 用例当前方法名
    private String methodName;

    // 当前用例文件夹名
    private String csvFolderPath;

    // 当前用例驱动csv文件名
    private String csvFilePath;

    // csv入参名称列表
    private List<String> parameterKeyList;

    // 当前用例驱动yaml文件名，可以为空
    private String yamlPath;

    // Yaml加载对象
    private YamlTestData yamlTestData;

    /**
     *
     *
     * @return property value of className
     */
    public String getClassName() {
        return className;
    }

    /**
     *
     *
     * @param className value to be assigned to property className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     *
     *
     * @return property value of methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     *
     *
     * @param methodName value to be assigned to property methodName
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     *
     *
     * @return property value of csvFolderPath
     */
    public String getCsvFolderPath() {
        return csvFolderPath;
    }

    /**
     *
     *
     * @param csvFolderPath value to be assigned to property csvFolderPath
     */
    public void setCsvFolderPath(String csvFolderPath) {
        this.csvFolderPath = csvFolderPath;
    }

    /**
     *
     *
     * @return property value of csvFilePath
     */
    public String getCsvFilePath() {
        return csvFilePath;
    }

    /**
     *
     *
     * @param csvFilePath value to be assigned to property csvFilePath
     */
    public void setCsvFilePath(String csvFilePath) {
        this.csvFilePath = csvFilePath;
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
     * @return property value of parameterKeyList
     */
    public List<String> getParameterKeyList() {
        return parameterKeyList;
    }

    /**
     *
     *
     * @param parameterKeyList value to be assigned to property parameterKeyList
     */
    public void setParameterKeyList(List<String> parameterKeyList) {
        this.parameterKeyList = parameterKeyList;
    }
}
