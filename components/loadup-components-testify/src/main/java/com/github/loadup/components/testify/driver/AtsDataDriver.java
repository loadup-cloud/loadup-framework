/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.driver;

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

import com.github.loadup.components.testify.context.TestifySuiteContext;
import com.github.loadup.components.testify.context.TestifySuiteContextHolder;
import com.github.loadup.components.testify.driver.annotation.TestData;
import com.github.loadup.components.testify.driver.model.DriverDataProvider;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.util.FileUtil;
import com.github.loadup.components.testify.yaml.YamlTestData;
import com.github.loadup.components.testify.yaml.YamlTestUtil;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

/**
 * 测试驱动基类（简化版）
 *
 *
 *
 */
@Slf4j
public class AtsDataDriver extends AbstractTestNGSpringContextTests {

    /**
     * add dependency jar that sofa runtime needed
     */
    static {
        initTestArtifact("ats-common-,ats-api-,opencsv-,ats-db-component-");
    }

    protected static void initTestArtifact(String names) {
        String oldProperties = System.getProperty("test_artifacts");
        if (StringUtils.isNotBlank(oldProperties)) {
            oldProperties += ",";
        } else {
            oldProperties = "";
        }
        System.setProperty("test_artifacts", oldProperties + names);
    }

    /**
     * 基于类名生成csv文件夹名
     *
     * @param className
     * @return
     */
    private static String getCSVFolderByClassName(String className) {
        if (className.indexOf("NormalTest") != -1) {
            return "testres/normal/" + className.split("NormalTest")[0] + "/";
        } else if (className.indexOf("FuncExceptionTest") != -1) {
            return "testres/funcExp/" + className.split("FuncExceptionTest")[0] + "/";
        } else if (className.indexOf("SysExceptionTest") != -1) {
            return "testres/sysExp/" + className.split("SysExceptionTest")[0] + "/";
        } else {
            return "";
        }
    }

    /**
     * 初始化配置数据
     *
     * @throws Exception
     */
    @BeforeClass
    protected void setUp() {
        AtsConfiguration.loadAtsProperties();
        AtsConfiguration.loadDBConfiguration();
        // set the runtime field for bean get support
        // SofaRunTimeContextHolder.set(con);
    }

    /**
     * CsvDataProvider
     * csv file choose algorithm:
     * 1.default( on test method without TestData annotation)
     * a). normaltest,eg: test class file XxxNormalTest.java
     * filepath=src/test/resources/testres/normal/Xxx/XxxNormalTest.methodname.csv
     * b). exceptiontest,eg: test case file XxxFuncExceptionTest.java
     * filepath=src/test/resources/testres/funcExp/Xxx/XxxFuncExceptionTest.methodname.csv
     * 2. test method with TestData annotation, use custom data driver file
     * filepath=TestData annotation's path value + TestData annotation's fileName
     *
     * @param method test method(with @Test annotation)
     * @return Data Driver Provider
     * @throws IOException
     */
    @DataProvider(name = "CsvDataProvider")
    public Iterator<?> getDataProvider(Method method) throws IOException {
        Class<?> cls = method.getDeclaringClass();
        String className = cls.getSimpleName();
        String csvFolderPath;
        String csvFilePath;
        TestifyLogUtil.checkLogFolder(className);
        TestifySuiteContext context = new TestifySuiteContext();

        if (null != method.getAnnotation(TestData.class)) {
            csvFolderPath = method.getAnnotation(TestData.class).path();
            csvFilePath = csvFolderPath + method.getAnnotation(TestData.class).fileName();
        } else {
            csvFolderPath = getCSVFolderByClassName(className);
            csvFilePath = csvFolderPath + className + "." + method.getName() + ".csv";
        }
        context.setClassName(className);
        context.setCsvFilePath(csvFilePath);
        context.setCsvFolderPath(csvFolderPath);
        context.setMethodName(method.getName());
        String yamlPath = csvFilePath.substring(0, csvFilePath.length() - 4) + ".yaml";
        if (YamlTestUtil.isSingleYaml()) {
            context.setYamlPath(yamlPath);
            context.setYamlTestData(new YamlTestData(FileUtil.getTestResourceFile(yamlPath)));
        }
        TestifySuiteContextHolder.set(context);
        TestifyLogUtil.info(log, "Driver Data file path : " + csvFilePath);
        return new DriverDataProvider(cls, method, csvFilePath);
    }
}
