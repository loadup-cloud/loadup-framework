package com.alipay.test.acts.context;

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

import org.testng.Assert;

public class ActsSuiteContextHolder {

    //线程上下文
    private static ThreadLocal<ActsSuiteContext> actsSuiteContextLocal = new ThreadLocal<ActsSuiteContext>();

    /**
     * 取得操作上下文
     *
     * @return 上下文
     */
    public static ActsSuiteContext get() {
//        Assert.assertNotNull("测试脚本上下文不存在", actsSuiteContextLocal.get());
        return actsSuiteContextLocal.get();
    }

    /**
     * 检查当前是否存在上下文
     *
     * @return true如果当前是否存在上下文，否则false
     */
    public static boolean exists() {
        return actsSuiteContextLocal.get() != null;
    }

    /**
     * 设置上下文
     *
     * @param context 上下文
     */
    public static void set(ActsSuiteContext context) {
        actsSuiteContextLocal.set(context);
    }

    /**
     * 清理上下文
     */
    public static void clear() {
        actsSuiteContextLocal.remove();
    }
}
