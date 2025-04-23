/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.manage;

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
import java.util.List;



import com.alipay.test.acts.component.enums.RepositoryType;
import com.alipay.test.acts.manage.enums.LoggerType;
import com.alipay.test.acts.manage.log.LoggerFactory;
import com.alipay.test.acts.support.TestCaseHolder;
import org.apache.commons.lang3.StringUtils;

/**
 * 测试内容记录器，以摘要日志形式输出。
 * <p>
 * 比如：记录测试用例调用的组件。
 * @author liudian.li
 * @version $Id: TestRecorder.java, v 0.1 2015-8-10 下午8:59:23 liudian.li Exp $
 */
public class TestRecorder {

    private static final ThreadLocal<List<String>> componentList = new ThreadLocal<List<String>>();

    static {
        componentList.set(new ArrayList<String>());
    }

    /**
     * 清空记录的组件
     */
    public static void clear() {
        componentList.set(new ArrayList<String>());
    }

    /**
     * 添加组件调用记录
     * 
     * @param typeId       组件类型
     * @param componentId  组件ID
     */
    public static void useComponent(RepositoryType typeId, String componentId) {
        component(typeId.toString() + ":" + componentId);
    }

    private static void component(String component) {
        List<String> list = componentList.get();
        list.add(component);
    }

    /**
     * 检测是否有组件调用记录
     * 
     * @return
     */
    public static boolean hasComponent() {
        List<String> list = componentList.get();
        if (list != null && list.size() > 0)
            return true;
        return false;
    }

    /**
     * 输出用例调用组件的记录日志
     */
    public static void record() {

        String caseId = TestCaseHolder.get() == null ? null : TestCaseHolder.get().getCaseId();

        LoggerFactory.getLogger(LoggerType.ACTS_DIEGEST).info(getDigest(caseId));
    }

    /**
     * 输出组件调用记录，并清空记录
     * 
     * @param prefix 
     * @return
     */
    public static String getDigest(String prefix) {

        prefix = StringUtils.isEmpty(prefix) ? "[-]" : "[" + prefix + "]";

        StringBuilder sb = new StringBuilder(prefix);
        List<String> list = componentList.get();
        sb.append("[");
        sb.append(StringUtils.join(list, ','));
        sb.append("]");

        clear();

        return sb.toString();
    }

}
