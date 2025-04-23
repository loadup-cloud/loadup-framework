package com.alipay.test.acts.data;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alipay.test.acts.helper.CSVHelper;
import com.alipay.test.acts.util.FileOperateUtils;
import com.alipay.test.acts.util.FullTableAnalysis;

/**
 * 数据模版生成工具
 * @author xiaoleicxl
 * @version $Id: dbTableModelUtil.java, v 0.1 2015年10月22日 下午10:20:08 xiaoleicxl Exp $
 */
public class BbTableModelUtil {

    private static final Log LOG = LogFactory.getLog(CSVHelper.class);

    public static void main(String[] args) {
        generateAppTableModel("/Users/xiaoleicxl/workspace-alibank/fcfinancecore/branch/0825");
    }

    /**
     * 生成应用的所有表模版
     * @param appRoot
     * @param dbType 支持三种 
     */
    public static HashSet<String> generateAppTableModel(String appRoot) {

        // 1. 获取dal-config.xml;
        List<File> dalConfigFileList = new ArrayList<File>();

        // PathUtil.getAppPath()
        FileOperateUtils.findFileRecursive(appRoot, "dal-config.xml", dalConfigFileList);

        // 2. 解析dal-config.xml获取系统全量表
        HashSet<String> tableSet = new HashSet<String>();

        for (File dalConfigXml : dalConfigFileList) {
            tableSet.addAll(FullTableAnalysis.getFullTableByDalConfig(dalConfigXml));
        }
        return tableSet;

    }
}
