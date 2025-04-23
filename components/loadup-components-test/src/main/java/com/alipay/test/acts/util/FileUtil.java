package com.alipay.test.acts.util;

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

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.api.LogApis;
import com.alipay.test.acts.context.ActsSuiteContextHolder;
import com.alipay.test.acts.log.ActsLogUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 用于处理文件相关内容
 *
 * @author baishuo.lp
 * @version $Id: FileUtil.java, v 0.1 2015年2月8日 上午1:51:39 baishuo.lp Exp $
 */
public class FileUtil {
    private static final Log LOG = LogFactory.getLog(FileUtil.class);

    private static final String DEFAULT_PATH = "/src/test/resources/";

    private static String DIR_PATH = System.getProperty("user.dir");

    /**
     * 基于Test Bundle，从src/test/resources中获取相对路径文件引用
     *
     * @param fileRelativePath
     * @return
     */
    public static File getTestResourceFile(String fileRelativePath) {
        String fileFullPath = DIR_PATH + DEFAULT_PATH + fileRelativePath;
        File file = new File(fileFullPath);
        return file;
    }

    /**
     * 基于Test Bundle，从绝对路径中获取相对路径文件引用
     *
     * @param fileRootPath
     * @return
     */
    public static File getTestResourceFileByRootPath(String fileRootPath) {
        if (StringUtils.isBlank(fileRootPath)) {
            return null;
        }
        File file = new File(fileRootPath);
        return file;
    }

    /**
     * 从相对路径读取文件
     *
     * @param relatePath
     * @return
     */
    public static String readFile(String relatePath) {
        try {
            InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(relatePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            ActsLogUtil.error(LOG, "从" + relatePath + "读取数据失败", e);
            return null;
        }
    }

    /**
     * 基于原路径，修正跳转csv路径为test bundle下绝对路径
     * 优先使用驱动文件夹下csv路径，其次test bundle下相对路径
     *
     * @param targetFilePath
     * @param originFilePath
     * @return
     */
    public static String getRelativePath(String targetFilePath, String originFilePath) {
        if (StringUtils.isBlank(originFilePath)) {
            originFilePath = "";
        } else {
            originFilePath = originFilePath.contains("/")
                    ? originFilePath.substring(0, originFilePath.lastIndexOf("/") + 1) : "";
        }
        String finalFilePath = originFilePath + targetFilePath;
        File file = new File(finalFilePath);
        if (file.exists()) {
            //test bundle下相对路径
            return finalFilePath;
        } else {
            if (ActsSuiteContextHolder.exists()) {
                finalFilePath = ActsSuiteContextHolder.get().getCsvFolderPath() + targetFilePath;
                file = getTestResourceFile(finalFilePath);
                if (file.exists())
                    //驱动文件夹相对路径
                    return finalFilePath;
            }
            file = getTestResourceFile(targetFilePath);
            if (file.exists()) {
                //驱动文件夹旧相对路径
                return targetFilePath;
            } else {
                ActsLogUtil.fail(LOG, "无法找到对应路径，targetFilePath = " + targetFilePath
                        + ", originFolderPath = " + originFilePath);
            }
        }
        return null;
    }

    /**
     * 打印字符串到新文件
     * mode=-1: 文件存在则跳过
     * mode=0: 文件存在则添加分隔符续写
     * mode=1: 文件存在则覆盖
     *
     * @param file
     * @param fileContent
     * @return
     */
    public static boolean writeFile(File file, String fileContent, int mode) {
        try {
            if (file.exists()) {
                switch (mode) {
                    case -1:
                        ActsLogUtil.debug(LOG, "文件已经存在,跳过");
                        return true;
                    case 0:
                        ActsLogUtil.debug(LOG, "文件已经存在,添加分隔符续写");
                        fileContent = "\n\n========================================================================\n\n"
                                + fileContent;
                        break;
                    case 1:
                        ActsLogUtil.debug(LOG, "文件已经存在,删除重写");
                        file.delete();
                        file.createNewFile();
                        break;
                    default:
                        return false;
                }
            } else {
                file.createNewFile();
            }
            Writer writer = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8");
            writer.write(fileContent);
            writer.close();
            ActsLogUtil.debug(LOG, "文件写成功");
            return true;
        } catch (Exception e) {
            ActsLogUtil.fail(LOG, "文件写失败", e);
            return false;
        }
    }

    /**
     * 读取yaml文件
     *
     * @param yamlFile
     * @return
     */
    public static LinkedHashMap<?, ?> readYaml(File yamlFile) {
        InputStream is = null;
        try {
            is = new FileInputStream(yamlFile);
            InputStreamReader reader = new InputStreamReader(is);
            Iterator<Object> iterator = new Yaml().loadAll(reader).iterator();
            LinkedHashMap<?, ?> rawData = (LinkedHashMap<?, ?>) iterator.next();
            return rawData;
        } catch (FileNotFoundException e) {
            LogApis.warn("找不到对应文件" + yamlFile.getAbsolutePath());
            return null;
        } catch (Exception e) {
            LogApis.warn("文件格式有误" + yamlFile.getAbsolutePath());
            return null;
        }
    }

    /**
     * Setter method for property <tt>dIR_PATH</tt>.
     *
     * @param dIR_PATH value to be assigned to property dIR_PATH
     */
    public static void setDIR_PATH(String dIR_PATH) {
        DIR_PATH = dIR_PATH;
    }

    public static File findDbModelPath(File file) {

        String fileName = "dbModel";
        return findModelPath(file, fileName);
    }

    public static File findObjModelPath(File file) {

        String fileName = "objModel";
        return findModelPath(file, fileName);
    }

    /**
     * @param file
     * @param fileName
     * @return
     */
    private static File findModelPath(File file, String fileName) {
        File result = null;
        List<File> dalConfigFileList = new ArrayList<File>();
        FileOperateUtils.findFolderRecursive(file.getAbsolutePath(), fileName, dalConfigFileList);

        for (File f : dalConfigFileList) {

            if (f.getAbsolutePath().contains("target")) {
                continue;
            } else {
                result = f;
                break;
            }
        }
        return result;
    }

    /**
     * 兼容FlyTest云端传测试包的读取测试数据方式
     *
     * @param classLoader
     * @param filePath
     * @return
     */
    public static String getFilePathByResource(ClassLoader classLoader, String filePath) {
        URL url = classLoader.getResource(filePath);
        return url == null ? filePath : url.getFile();
    }
}
