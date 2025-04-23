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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.util.FileOperateUtils;



/**
 * 获取选中bundle的bundleName
 * @author xiaoleicxl
 * @version $Id: MFfileUtil.java, v 0.1 2015年9月25日 下午3:21:15 xiaoleicxl Exp $
 */
public class MFfileUtil {

    public static String getModuleName(File file) {
        InputStream is;
        try {
            is = new FileInputStream(file);

            Manifest mf = new Manifest(is);
            Attributes attributes = mf.getMainAttributes();
            String bundleSymbolicNameString = attributes.getValue("Module-Name") == null ? attributes
                .getValue("Bundle-SymbolicName") : attributes.getValue("Module-Name");
            bundleSymbolicNameString = (bundleSymbolicNameString.split(";"))[0].trim();//去掉singleton:=true部分
            return bundleSymbolicNameString;

        } catch (Exception e) {
            //log.error("", e);
        }
        return null;
    }

 
    public static String getAppPath(String prjPath, String prjName) {

        String sepratorString = "app";

        String appPath = "";
        if (!StringUtils.equals(StringUtils.substringAfterLast(prjPath, sepratorString), "")) {
            //sofa4用app分割
            appPath = StringUtils.substringBeforeLast(prjPath, sepratorString);
        } else {
            //sofa3用系统名分割
            sepratorString = (prjName.split("-"))[0];
            appPath = StringUtils.substringBeforeLast(prjPath, sepratorString);
        }

        return appPath;
    }
    /*
    * 获取指定命名规则的File
    * @author xiaoleicxl
    * @param scriptPath
    * @param scriptFileNameRegex
    * @return
    */
   public static java.util.List<File> getSpeciFile(String scriptPath, String scriptFileNameRegex) {

       java.util.List<File> scriptFileList = new ArrayList<File>();
       java.util.List<File> scriptFileListAfterFilt = new ArrayList<File>();

       FileOperateUtils.findFileRecursive(scriptPath, scriptFileNameRegex, scriptFileList);

       for (File f : scriptFileList) {
           if (!f.getAbsolutePath().contains("target")) {
               scriptFileListAfterFilt.add(f);
           }
       }

       return scriptFileListAfterFilt;
   }
}
