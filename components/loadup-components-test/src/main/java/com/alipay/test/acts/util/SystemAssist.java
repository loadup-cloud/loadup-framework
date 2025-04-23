/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 系统统一工具类。
 * 
 * @author dasong.jds
 * @version $Id: SystemAssist.java, v 0.1 2015年11月2日 下午6:48:04 dasong.jds Exp $
 */
public abstract class SystemAssist {

    /** 系统域 */
    public static final String[] SYS_DOMAINS  = new String[] { "ipay", "alipay" };

    /** 系统前缀 */
    public static final String[] SYS_PREFIXES = new String[] { "fc" };

    /**
     * 获取所有可能的包目录。
     * 
     * 
     * @param system
     * @return
     */
    public static List<String> getPossiblePackageList(String system) {

        if (system == null) {
            return Collections.emptyList();
        }

        List<String> packages = new ArrayList<String>();

        for (String domain : SYS_DOMAINS) {
            String com = "com." + domain + ".";
            packages.add(com + system);

            for (String prefix : SYS_PREFIXES) {
                if (system.startsWith(prefix)) {
                    packages.add(com + prefix + "." + system.substring(prefix.length()));
                }
            }
        }

        return packages;
    }
}
