/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.log;

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
import com.github.loadup.components.testify.constant.TestifyYamlConstants;
import com.github.loadup.components.testify.context.TestifyCaseContext;
import com.github.loadup.components.testify.context.TestifyCaseContextHolder;
import com.github.loadup.components.testify.context.TestifySuiteContext;
import com.github.loadup.components.testify.context.TestifySuiteContextHolder;
import com.github.loadup.components.testify.driver.enums.SuiteFlag;
import com.github.loadup.components.testify.util.FileUtil;
import com.github.loadup.components.testify.util.JsonUtil;
import com.github.loadup.components.testify.util.LogUtil;
import com.github.loadup.components.testify.yaml.YamlTestData;
import com.github.loadup.components.testify.yaml.YamlTestUtil;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.testng.Assert;

/**
 * ActsLogUtil 测试日志文件操作类
 */
@Slf4j
public class TestifyLogUtil {

    /**
     * 基于脚本类名，进行日志模型文件夹准备，并清空之前上下文日志模型
     *
     * @param className
     */
    public static void checkLogFolder(String className) {
        Assert.assertNotNull("类名不能为空", className);

        // 创建LogFolder
        File logFolder = new File(TestifyPathConstants.LOG_FOLDER_PATH);
        if (!logFolder.exists() || !logFolder.isDirectory()) {
            if (!logFolder.mkdir()) {
                log.error(TestifyPathConstants.LOG_FOLDER_PATH + "文件夹无法创建");
                return;
            }
        }

        // 根据脚本类名创建日志文件夹
        File classFolder = new File(TestifyPathConstants.LOG_FOLDER_PATH + "/" + className);
        if (!classFolder.exists() || !classFolder.isDirectory()) {
            if (!classFolder.mkdir()) {
                log.error(TestifyPathConstants.LOG_FOLDER_PATH + "/" + className + "文件夹无法创建");
                return;
            }
        }
    }

    /**
     * 基于类名，方法名及当前用例入参，准备线程日志上下文
     *
     * @param parameters
     */
    public static void initLogContext(Object[] parameters) {
        //        Assert.assertNotNull("参数不会为空", parameters);
        TestifySuiteContext suiteContext = TestifySuiteContextHolder.get();
        List<String> parameterList = suiteContext.getParameterKeyList();

        String caseId = null;
        String caseDesc = null;
        SuiteFlag suiteFlag = null;
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        for (int i = 0; i < parameterList.size(); i++) {
            if (parameterList.get(i).equalsIgnoreCase("caseId")) {
                caseId = (String) parameters[i];
            } else if (parameterList.get(i).equalsIgnoreCase("description")) {
                caseDesc = (String) parameters[i];
            } else if (parameterList.get(i).equalsIgnoreCase("suiteflag")) {
                suiteFlag = SuiteFlag.getByCode((String) parameters[i]);
            }
            parameterMap.put(parameterList.get(i), parameters[i]);
        }
        TestifyCaseContext context = new TestifyCaseContext();
        context.setCaseId(caseId);
        context.setCaseDesc(caseDesc);
        context.setParameterMap(parameterMap);
        context.setSuiteFlag(suiteFlag);
        if (!YamlTestUtil.isSingleYaml()) {
            String caseYamlPath = suiteContext.getCsvFolderPath() + context.getCaseId() + ".yaml";
            String commonYamlPath = suiteContext.getCsvFolderPath() + TestifyYamlConstants.COMMONKEY + ".yaml";
            context.setYamlPath(caseYamlPath);
            YamlTestData caseData = new YamlTestData(FileUtil.getTestResourceFile(caseYamlPath));
            File commonFile = FileUtil.getTestResourceFile(commonYamlPath);
            if (commonFile.exists()) {
                YamlTestData commonData = new YamlTestData(commonFile);
                caseData.getTestCaseMap().putAll(commonData.getTestCaseMap());
            }
            context.setYamlTestData(caseData);
        }

        TestifyCaseContextHolder.set(context);
        if (log.isInfoEnabled()) {
            log.info("\n================================================\n");
            log.info("开始执行" + suiteContext.getClassName() + "类" + suiteContext.getMethodName() + "方法用例:"
                    + context.getCaseId() + "," + context.getCaseDesc());
        }
    }

    /**
     * 基于线程日志上下文，生成日志文件，并清理上下文
     */
    public static void clearLogContext() {
        TestifySuiteContext suiteContext = TestifySuiteContextHolder.get();
        TestifyCaseContext caseContext = TestifyCaseContextHolder.get();

        String folderPath = TestifyPathConstants.LOG_FOLDER_PATH + "/" + suiteContext.getClassName() + "/";
        String caseId = caseContext.getCaseId();
        String filePath = folderPath + caseId + ".log";
        String logData = "脚本" + suiteContext.getClassName() + "方法" + suiteContext.getMethodName() + "测试日志:\n";
        logData += "==============脚本当前入参============\n";
        //        logData += JSON.toJSONString(caseContext.getParameterMap(), true)
        //                   + "\n==============以下为过程日志============\n";
        for (String data : caseContext.getLogData()) {
            logData += data + "\n";
        }
        File logFile = new File(filePath);
        FileUtil.writeFile(logFile, logData, 1);
        if (log.isInfoEnabled()) {
            log.info(suiteContext.getClassName() + "类" + suiteContext.getMethodName() + "方法用例" + caseContext.getCaseId()
                    + "执行完毕");
            log.info("\n================================================\n");
        }

        TestifyCaseContextHolder.clear();
    }

    /**
     * 生成baseline文件
     */
    public static void dumpBaseline() {
        try {
            TestifySuiteContext suiteContext = TestifySuiteContextHolder.get();
            String dumpData;
            String yamlFilePath;
            if (YamlTestUtil.isSingleYaml()) {
                dumpData = suiteContext.getYamlTestData().dump();
                yamlFilePath = suiteContext.getYamlPath();

            } else {
                TestifyCaseContext caseContext = TestifyCaseContextHolder.get();
                YamlTestData data = caseContext.getYamlTestData();
                data.getTestCaseMap().remove(TestifyYamlConstants.COMMONKEY);
                dumpData = data.dump();
                yamlFilePath = caseContext.getYamlPath();
            }
            String folderPath = TestifyPathConstants.LOG_FOLDER_PATH + "/" + suiteContext.getClassName() + "/";
            String fileName = FileUtil.getTestResourceFile(yamlFilePath).getName();

            File yamlFile = new File(folderPath + fileName);
            FileUtil.writeFile(yamlFile, dumpData, 1);
        } catch (Exception e) {
            log.error("生成Baseline失败", e);
        }
    }

    /**
     * 添加线程日志，不打印到console
     *
     * @param message
     */
    public static void addProcessLog(Object message) {
        if (TestifyCaseContextHolder.exists()) {
            List<String> processData = TestifyCaseContextHolder.get().getLogData();
            if (message instanceof String) processData.add((String) message);
            else {
                processData.add(JsonUtil.toPrettyString(message));
            }
        }
    }

    /**
     * 用error打印当前错误信息堆栈，并添加过程中异常日志，不打印到console
     *
     * @param message
     */
    public static void addProcessLog(String message, Throwable e) {
        if (TestifyCaseContextHolder.exists()) {
            List<String> processData = TestifyCaseContextHolder.get().getLogData();
            processData.add(message);
            String errMessage = LogUtil.getErrorMessage(e);
            processData.add(errMessage);
            List<String> processLog = TestifyCaseContextHolder.get().getProcessErrorLog();
            processLog.add(message);
            processLog.add(errMessage);
        }
    }

    /**
     * debug日志
     *
     * @param logger
     * @param message
     */
    public static void debug(Logger logger, String message) {
        if (logger.isDebugEnabled()) logger.debug(message);
        addProcessLog(message);
    }

    /**
     * info日志
     *
     * @param logger
     * @param message
     */
    public static void info(Logger logger, String message) {
        if (logger.isInfoEnabled()) logger.info(message);
        addProcessLog(message);
    }

    /**
     * warn日志
     *
     * @param logger
     * @param message
     */
    public static void warn(Logger logger, String message) {
        logger.warn(message);
        addProcessLog(message);
    }

    /**
     * error日志，无异常
     *
     * @param logger
     * @param message
     */
    public static void error(Logger logger, String message) {
        logger.error(message);
        addProcessLog(message);
        try {
            TestifyCaseContextHolder.get().getProcessErrorLog().add(message);
        } catch (Exception e) {

        }
    }

    /**
     * error日志，有异常
     *
     * @param logger
     * @param message
     * @param e
     */
    public static void error(Logger logger, String message, Throwable e) {
        logger.error(message, e);
        addProcessLog(message, e);
    }

    /**
     * 打印error日志并失败，无异常
     *
     * @param logger
     * @param message
     */
    public static void fail(Logger logger, String message) {
        logger.error(message);
        addProcessLog(message);
        Assert.fail(message);
    }

    /**
     * 打印error日志并失败，有异常
     *
     * @param logger
     * @param message
     * @param e
     */
    public static void fail(Logger logger, String message, Throwable e) {
        logger.error(message, e);
        addProcessLog(message, e);
        Assert.fail(message);
    }
}
