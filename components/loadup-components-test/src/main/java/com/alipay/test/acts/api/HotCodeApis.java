///**
// * Alipay.com Inc.
// * Copyright (c) 2004-2015 All Rights Reserved.
// */
//package com.alipay.test.acts.api;
//
///*-
// * #%L
// * loadup-components-test
// * %%
// * Copyright (C) 2022 - 2025 loadup_cloud
// * %%
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// * #L%
// */
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map.Entry;
//
//import org.dom4j.Document;
//import org.dom4j.DocumentHelper;
//import org.dom4j.Element;
//import org.dom4j.io.OutputFormat;
//import org.dom4j.io.SAXReader;
//import org.dom4j.io.XMLWriter;
//
//import com.alipay.test.acts.manage.enums.LoggerType;
//import com.alipay.test.acts.manage.log.Logger;
//import com.alipay.test.acts.manage.log.LoggerFactory;
//
///**
// * hotcode 生成workspace.xml的辅助工具类
// * Thanks to feiyue.lfy
// *
// * @author zhiyuan.lzy
// * @version $Id: HotCodeApis.java, v 0.1 2015年11月3日 上午11:36:49 zhiyuan.lzy Exp $
// */
//public class HotCodeApis {
//    //存储项目bundle的key,和路径
//    private static HashMap<String, String> map            = new HashMap<String, String>();
//
//    //testbundle的路径
//    private static String                  testBundlePath = null;
//
//    //项目的跟路径
//    private static String                  workspacePath  = null;
//
//    //日志
//    private static Logger                  logger         = LoggerFactory
//                                                              .getLogger(LoggerType.ACTS_LOG);
//
//    /**
//     * 需要传入的路径是test工程的路径,比如D:/3.SvnCode/0915_fcassetflux/app/test
//     *
//     * @param projectPath
//     * @throws IOException
//     */
//    public static void generate(File projectPath) throws IOException {
//        testBundlePath = projectPath.getCanonicalPath().replace(File.separator, "/");
//        workspacePath = new File(testBundlePath).getParentFile().getParent()
//            .replace(File.separator, "/");
//        long StartTime = System.currentTimeMillis();
//        logger.info("Starting Analyze the workspace...");
//
//        genFile(workspacePath);
//
//        logger
//            .debug("The whole lifecircle used " + (System.currentTimeMillis() - StartTime) + "ms");
//    }
//
//    /***
//     * 真正的生成workspace.xml的地方
//     *
//     * @param path
//     */
//    private static void genFile(String path) {
//        logger.info("workspacePath is " + workspacePath);
//        logger.info("testBundlePath is " + testBundlePath);
//
//        refreshFileList(path);
//
//        createXml(map, testBundlePath);
//    }
//
//    /***
//     * 扫描工程目录,进行map填充
//     *
//     * @param strPath
//     */
//    private static void refreshFileList(String strPath) {
//        File dir = new File(strPath);
//        File[] files = dir.listFiles();
//
//        if (files == null)
//            return;
//        for (int i = 0; i < files.length; i++)
//            if (files[i].isDirectory()) {
//                refreshFileList(files[i].getAbsolutePath());
//            } else {
//                String strFileName = files[i].getAbsolutePath().toLowerCase();
//                if (strFileName.endsWith("pom.xml")) {
//                    String filepath = ConcatPath(files[i].getParent(), new String[] { "target",
//                            "classes" });
//
//                    // if (new File(filepath).exists()) {
//                    String key = parserPomXml(strFileName);
//                    String value = filepath.toString().replace(File.separator, "/");
//                    if (null != key) {
//                        if (key.endsWith("test")) {
//                            value = value.substring(0, value.lastIndexOf("classes")).concat(
//                                "test-classes");
//                        }
//
//                        value = value.replace(workspacePath, "${project_loc}/../..");
//
//                        logger.info("---" + strFileName);
//                        logger.info("------" + key);
//                        logger.info("------" + value);
//                        map.put(key, value);
//                    }
//                    // }
//                }
//            }
//    }
//
//    /**
//     * 根据map填写test bundle
//     *
//     * @param map
//     * @param testBundlePath
//     */
//    private static void createXml(HashMap<String, String> map, String testBundlePath) {
//        logger.info("Starting createXml...");
//
//        if (null == testBundlePath) {
//            logger.error("Can not find the testBundle");
//            return;
//        }
//        Document document = DocumentHelper.createDocument();
//        Element root = document.addElement("workspace");
//        Element deploy = root.addElement("deploy");
//        for (Entry<String, String> entry : map.entrySet()) {
//            deploy.addElement("mapping").addAttribute("src", entry.getKey())
//                .addAttribute("dest", entry.getValue());
//        }
//
//        try {
//            OutputFormat format = OutputFormat.createPrettyPrint();
//            format.setEncoding("UTF-8");
//            XMLWriter writer = new XMLWriter(new FileWriter(ConcatPath(testBundlePath,
//                new String[] { "workspace.xml" })), format);
//
//            writer.write(document);
//            writer.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        logger.info("End createXml...");
//    }
//
//    private static String parserPomXml(String fileName) {
//        logger.info("Starting parse pom xml: " + fileName);
//
//        SAXReader reader = new SAXReader();
//        try {
//            Document document = reader.read(fileName);
//            Element rootElm = document.getRootElement();
//            String artifactId = rootElm.elementText("artifactId");
//            String version = rootElm.elementText("version");
//
//            if (null != artifactId) {
//                if (artifactId.endsWith("test")) {
//                    return artifactId;
//                }
//                return artifactId + "-" + version + ".jar";
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        logger.info("End parse pom xml: " + fileName);
//        return null;
//    }
//
//    private static String ConcatPath(String dir, String[] files) {
//        if (null == dir) {
//            logger.error("ConcatPath error, dir is null");
//            return null;
//        }
//        StringBuilder path = new StringBuilder(dir);
//        for (String file : files) {
//            if (path.toString().endsWith(File.separator))
//                path.append(file).toString();
//            else {
//                path.append(File.separator).append(file).toString();
//            }
//        }
//        return path.toString();
//    }
//
//}
