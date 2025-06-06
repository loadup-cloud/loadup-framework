/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.template;

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

import java.io.*;
import java.util.logging.Logger;

/**
 *
 *
 */
public class InitTestBundle {

    public static String basePath = System.getProperty("user.dir");

    public static String CONFIG_PATH = basePath + "\\src\\main\\resources\\initTetsBundle";

    public static String SERVICE_TEST_PATH = basePath + "\\src\\main\\resources\\initTetsBundle\\servicetest";

    public static String SOFA_CONFIG_PATH = basePath + "\\src\\main\\resources\\initTetsBundle\\sofaconfig";

    private static Logger logger = Logger.getLogger("InitTestBundle");

    private static InitTestBundle init = new InitTestBundle();

    public InitTestBundle() {
        start();
    }

    /**
     * 返回对象
     *
     * @return
     */
    public static InitTestBundle getInstance() {
        return init;
    }

    public static void main(String[] paramArrayOfString) {
        try {
            InitTestBundle s = new InitTestBundle();
            s.start();
        } catch (Exception e) {

        }
    }

    /**
     * 生成测试bundle 过程执行方法
     */
    public void start() {
        logger.info("====================start===================");
        try {
            // 生成测试基类
            String linuxPath = "\\\\100.81.64.60\\home";
            String str2 = "D:\\initTestBundleTest";
            genFileBase("生成测试基类", linuxPath, str2);
            genFileBase("生成测试resource", linuxPath, str2);

        } catch (Exception e) {

        }
        logger.info("=====================end====================");
        logger.info("==生成成功，请到该项目下执行mvn eclipse:eclipse,再导入eclipse即可==");
    }

    /**
     * 测试生成操作类
     *
     * @throws IOException
     */
    private void genFileBase(String stepMemo, String copyPath, String isCopyPath) throws IOException {
        logger.info("++++++++++【1】" + stepMemo + "++++++++++");

        File localFile = new File(isCopyPath);
        if (!localFile.exists()) {
            throw new RuntimeException("目录不存在!");
        }
        String str3 = "xcopy " + copyPath + " " + isCopyPath + " /e /y /d";
        runCMD(str3);
    }

    /**
     * 生成基础文件
     *
     * @throws IOException
     */
    private void genTestFile(String path, String fileName, String input) throws IOException {

        // 判断文件路径是否存在，不存在新建
        File newFileDir = new File(path);

        if (!newFileDir.exists() && !newFileDir.isDirectory()) {
            newFileDir.mkdir();
        }
        // 判断问价是否存在不存在则新建
        File file = new File(path + "//" + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        WriteStringToFile(path + "//" + fileName, input, "UTF-8");
    }

    /**
     * 指定文件写入
     *
     * @param filePath
     * @param addString
     * @param tableName
     * @param isMemo
     */
    public void WriteStringToFile(String filePath, String addString, String encoding) {
        try {
            //  FileWriter fw = new FileWriter(filePath);
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(filePath, true), encoding);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(addString + "\r\n "); // 往已有的文件上添加字符串
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 运行CMD命令执行copy
     *
     * @param paramString
     */
    private void runCMD(String paramString) {
        try {
            logger.info("exec cmd:" + paramString);
            Process localProcess = Runtime.getRuntime().exec(paramString);
            InputStream localInputStream = localProcess.getInputStream();
            while (localInputStream.read() != -1) {}
            localInputStream.close();
            localProcess.waitFor();
        } catch (Exception localException) {
            throw new RuntimeException("exec cmd failed,cmd:" + paramString);
        }
    }
}
