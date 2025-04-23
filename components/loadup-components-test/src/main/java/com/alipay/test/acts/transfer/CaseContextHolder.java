/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.alipay.test.acts.transfer;

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

import java.util.HashMap;
import java.util.Map;

/**
 * 测试用例相关信息上下文
 * 
 * @author xiangyu.chen
 * @version $Id: CaseContextHolder.java, v 0.1 2015年10月10日 下午2:44:57 xiangyu.chen Exp $
 */
public class CaseContextHolder {

    private static ThreadLocal<Map<String, String>> caseContextLocal = new ThreadLocal<Map<String, String>>();

    private static final String                     CASEID           = "caseId";

    private static final String                     DESC             = "desc";

    /**
     * 
     */
    public CaseContextHolder() {
        super();
    }

    /**
     * 
     * @return
     */
    public static Map<String, String> get() {
        return caseContextLocal.get();
    }

    /**
     * 
     * @param s
     */
    public static void put(String caseId, String desc) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(CASEID, caseId);
        map.put(DESC, desc);
        caseContextLocal.set(map);
    }

    /**
     * 
     */
    public static void clear() {
        caseContextLocal.set(null);
    }
}
