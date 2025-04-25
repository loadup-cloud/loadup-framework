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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.github.loadup.components.testify.api.LogApis;

/**
 * 
 *
 */
public class YamlTestData {

    /**
     * TestCase Map
     */
    private final Map<String, YamlTestCase> testCaseMap = new LinkedHashMap<String, YamlTestCase>();

    /**
     * 文件路径
     */
    private String filePath;

    //~~~ 构造方法
    @SuppressWarnings("unchecked")
    public YamlTestData(File yamlFile) {
        this.filePath = yamlFile.getAbsolutePath();

        /** 读取Yaml文件*/
        InputStream is = null;
        try {
            is = new FileInputStream(yamlFile);
        } catch (FileNotFoundException e) {
            LogApis.fail("找不到对应文件" + filePath);
        }
        if (is == null) {
            LogApis.fail("文件加载失败" + filePath);
        }
        InputStreamReader reader = new InputStreamReader(is);
        Iterator<Object> iterator = new Yaml().loadAll(reader).iterator();
        while (iterator.hasNext()) {
            LinkedHashMap<?, ?> rawData = (LinkedHashMap<?, ?>) iterator.next();
            for (Entry<?, ?> entry : rawData.entrySet()) {
                String caseId = (String) entry.getKey();
                Map<String, Object> testCaseData = (Map<String, Object>) entry.getValue();
                YamlTestCase testCase = new YamlTestCase(caseId, testCaseData);
                this.testCaseMap.put(caseId, testCase);
            }
        }
    }

    //~~~ 公用方法

    public YamlTestCase getTestCase(String caseId) {
        YamlTestCase testCase = this.testCaseMap.get(caseId);
        return testCase;
    }

    //~~~ 容器方法

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "YamlTestData [testCaseMap=" + testCaseMap + ", filePath=" + filePath + "]";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public String dump() {
        List caseList = new ArrayList();
        for (String caseId : this.getTestCaseMap().keySet()) {
            YamlTestCase testCase = this.getTestCase(caseId);
            Map caseData = testCase.dump();
            Map caseMap = new HashMap();
            caseMap.put(caseId, caseData);
            caseList.add(caseMap);
        }
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options).dumpAll(caseList.iterator());
    }

    /**
     * 
     *
     * @return property value of testCaseMap
     */
    public Map<String, YamlTestCase> getTestCaseMap() {
        return testCaseMap;
    }

    /**
     * 
     *
     * @return property value of filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     *
     *
     * @param filePath value to be assigned to property filePath
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
