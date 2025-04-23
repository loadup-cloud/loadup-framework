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
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 获取系统的表结构
 * @author xiaoleicxl
 * @version $Id: FullTableAnalysis.java, v 0.1 2015年10月15日 下午1:09:45 xiaoleicxl Exp $
 */
public class FullTableAnalysis {
    private static final Log LOG = LogFactory.getLog(FullTableAnalysis.class);

    /**
     * 通过dal-config.xml解析系统的表结构
     * @param dalConfigXml
     * @return
     */
    public static HashSet<String> getFullTableByDalConfig(File dalConfigXml) {

        HashSet<String> tableSet = new HashSet<String>();

        //1. 解析dalConfigXml
        DalConfigAnalysisHandler handler = new DalConfigAnalysisHandler();
        try {
            XMLParserUtil.parseXML(dalConfigXml, handler);
            return handler.getFullTables();
        } catch (Exception e) {
            LOG.error("解析dal-config.Xml出错！", e);
        }
        return tableSet;

    }

}
