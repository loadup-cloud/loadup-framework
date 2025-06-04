/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.data.model;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一键生成用例的时候,需要传递给规则库的条件
 *
 *
 *
 */
public class GenerateCondition {

    private String appName;

    private String methodName;

    // 用来标识是否插件已经预算了属性结果.
    private boolean isSpecAtom = false;

    private Map<String, List<Map<String, String>>> ruleMaps = new HashMap<String, List<Map<String, String>>>();

    private ClassLoader appSysLoader;

    /**
     *
     */
    public GenerateCondition() {
        super();
    }

    /**
     * @param appName    系统名
     * @param methodName 方法名
     */
    public GenerateCondition(String appName, String methodName) {
        super();
        this.appName = appName;
        this.methodName = methodName;
    }

    public ClassLoader getAppSysLoader() {
        return appSysLoader;
    }

    public void setAppSysLoader(ClassLoader appSysLoader) {
        this.appSysLoader = appSysLoader;
    }

    /**
     *
     *
     * @return property value of isSpecAtom
     */
    public boolean isSpecAtom() {
        return isSpecAtom;
    }

    /**
     *
     *
     * @param isSpecAtom value to be assigned to property isSpecAtom
     */
    public void setSpecAtom(boolean isSpecAtom) {
        this.isSpecAtom = isSpecAtom;
    }

    // 添加Map
    public void addRuleMap(String objName, List<Map<String, String>> mapList) {
        ruleMaps.put(objName, mapList);
    }

    public List<Map<String, String>> getRuleMaps(String objName) {
        return ruleMaps.get(objName);
    }

    /**
     *
     *
     * @return property value of ruleMaps
     */
    public Map<String, List<Map<String, String>>> getRuleMaps() {
        return ruleMaps;
    }

    /**
     *
     *
     * @param ruleMaps value to be assigned to property ruleMaps
     */
    public void setRuleMaps(Map<String, List<Map<String, String>>> ruleMaps) {
        this.ruleMaps = ruleMaps;
    }

    /**
     *
     *
     * @return property value of appName
     */
    public String getAppName() {
        return appName;
    }

    /**
     *
     *
     * @param appName value to be assigned to property appName
     */
    public void setAppName(String appName) {
        this.appName = appName;
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
}
