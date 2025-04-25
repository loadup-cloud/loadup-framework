
package com.github.loadup.components.testify.transfer;

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

/**
 * 
 *
 */

import com.github.loadup.components.testify.model.PrepareData;
import com.github.loadup.components.testify.util.BaseDataUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AppServiceInterceptor implements MethodInterceptor {
    private static final Logger Logger = LoggerFactory.getLogger(AppServiceInterceptor.class);

    //这里就是入参拦截到的地方,你可以写成D:/request.yaml,通过属性注入
    private String argYamlPath;

    //最后一个caseId的名称
    private String lastCaseId;

    //log日志的拦截路径
    private String logPath;

    //测试工程根路径
    private String testRootPath;

    private boolean firstFlag = true;

    /**
     * 指定文件写入,写入方式为追加写入
     *
     * @param filePath
     * @param addString
     * @param tableName
     * @param isMemo
     */
    public static void writeStringToFile(String filePath, String addString) {
        try {
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(filePath, true));
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(addString);
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(MethodInvocation innovation) throws Throwable {

        preProcess();

        Object[] args = innovation.getArguments();

        PrepareData prepareData = new PrepareData();

        Method method = innovation.getMethod();

        for (int i = 0; i < args.length; i++) {
            prepareData.getArgs().addArg(args[i]);
        }
        //将老脚本中的用例描述添加到新脚本中
        prepareData.setDescription(CaseContextHolder.get().get("desc"));

        Map<String, PrepareData> prepareDatas = new HashMap<String, PrepareData>();
        // 1. 这里CaseContextHolder.get().get("caseId")
        // 是当前测试用例的caseId,根据各个业务系统的不同,建议使用末尾提供的CaseContextHolder
        // 类,然后在现有的ats测试用例执行前放置一下.
        prepareDatas.put(CaseContextHolder.get().get("caseId"), prepareData);

        appendToYaml(prepareDatas, argYamlPath);

        if (CaseContextHolder.get().get("caseId").equalsIgnoreCase(lastCaseId)) {

            String finalResultPath = generateYamlPath(method);

            String caseIdPrefix = generateCasePrefix(method);

            TransferApi.transfer(argYamlPath, logPath, finalResultPath, caseIdPrefix);
        }

        return innovation.proceed();
    }

    /***
     * 首次预处理
     *
     * @return
     */
    private boolean preProcess() {
        Logger.info("进行首次文件删除操作,logPath=" + logPath + ",argYamlPath=" + argYamlPath);
        if (firstFlag) {

            File logFile = new File(logPath);
            File argsFile = new File(argYamlPath);

            if (logFile.exists()) {
                cleanFileContent(logFile);
            }
            if (argsFile.exists()) {
                cleanFileContent(argsFile);
            }
            firstFlag = false;
        }
        Logger.info("首次文件删除操作成功");
        return true;

    }

    /**
     *
     * @param file
     */
    private void cleanFileContent(File file) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            Logger.error(file + "不存在");
        }
        writer.print("");
        writer.close();
    }

    /**
     * 生成case前缀,生成结果:类名_方法名_CaseId_
     * @param method
     * @return
     */
    private String generateCasePrefix(Method method) {
        String caseIdPrefix = method.getDeclaringClass().getSimpleName() + "_" + method.getName()
                + "_" + CaseConstants.caseContainsStr;
        return caseIdPrefix;
    }

    public void appendToYaml(Map<String, PrepareData> prepareDatas, String file) {
        DumperOptions opts = new DumperOptions();
        Yaml yaml = new Yaml(new BaseDataUtil.MyRepresenter(opts));
        String str = yaml.dump(prepareDatas);
        writeStringToFile(file, str);
    }

    /***
     * 根据方法生成最终的yaml路径
     *
     * @param method
     * @return
     */
    private String generateYamlPath(Method method) {
        //获取方法名

        String methodName = method.getName();

        //获取类名

        String finalResultFile = consultYamlPath(methodName);

        return finalResultFile;

    }

    /**
     *
     * @param methodName
     * @param classFullName
     * @return
     */
    public String consultYamlPath(String methodName) {

        testRootPath = StringUtils.replace(testRootPath, "\\", "/");

        String finalResultPath = StringUtils.substringBefore(testRootPath, ".java");

        String finalResultFile = finalResultPath + "." + methodName + ".yaml";
        return finalResultFile;
    }

    /**
     * 
     *
     * @return property value of argYamlPath
     */
    public String getArgYamlPath() {
        return argYamlPath;
    }

    /**
     * 
     *
     * @param argYamlPath value to be assigned to property argYamlPath
     */
    public void setArgYamlPath(String argYamlPath) {
        this.argYamlPath = argYamlPath;
    }

    /**
     * 
     *
     * @return property value of lastCaseId
     */
    public String getLastCaseId() {
        return lastCaseId;
    }

    /**
     * 
     *
     * @param lastCaseId value to be assigned to property lastCaseId
     */
    public void setLastCaseId(String lastCaseId) {
        this.lastCaseId = lastCaseId;
    }

    /**
     * 
     *
     * @return property value of logPath
     */
    public String getLogPath() {
        return logPath;
    }

    /**
     * 
     *
     * @param logPath value to be assigned to property logPath
     */
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    /**
     * 
     *
     * @return property value of testRootPath
     */
    public String getTestRootPath() {
        return testRootPath;
    }

    /**
     * 
     *
     * @param testRootPath value to be assigned to property testRootPath
     */
    public void setTestRootPath(String testRootPath) {
        this.testRootPath = testRootPath;
    }

}
