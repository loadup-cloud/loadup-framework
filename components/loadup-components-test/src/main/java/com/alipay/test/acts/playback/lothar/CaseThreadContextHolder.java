/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.test.acts.playback.lothar;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 擦：sofa rpc client 中的 thread 和 业务执行不在一个线程中
 *
 * @author qingqin
 * @version $Id: CaseThreadContextHolder.java, v 0.1 2019年08月21日 下午8:16 qingqin Exp $
 */
@Deprecated
public class CaseThreadContextHolder {

    private static final Logger logger = LoggerFactory.getLogger(CaseThreadContextHolder.class);

    /*
     * caseId : report
     *
     * 一个线程下执行一个用例，直接拿出来就是当前用例执行的报告；这里为了便于理解，用 map 存放 case id 和 report 的映射
     *
     */
    private static final ThreadLocal<Map<String, String>> reports = new ThreadLocal<Map<String, String>>();

    public static Map<String, String> threadLocalReport() {
        return reports.get();
    }

    public static String get(String caseId) {

        logger.info("get thread local, current thread:" + Thread.currentThread().getName() + ", get:" + caseId);

        Map<String, String> context = reports.get();

        return null == context ? null : context.get(caseId);
    }

    public static void clear() {
        reports.remove();
    }

    public static void put(String caseId, String report) {

        Map<String, String> context = reports.get();

        if (null == context) {
            context = new HashMap<String, String>();
            reports.set(context);
        }

        context.put(caseId, report);

        logger.info("put thread local, current thread:" + Thread.currentThread().getName() + ", put:" + caseId);

    }

}
