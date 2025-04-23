/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.support;

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

/**
 * 测试执行模版。
 * <p>
 * 明确测试用例执行阶段，完成整个生命周期管理。
 * 
 * @author dasong.jds
 * @version $Id: TestTemplate.java, v 0.1 2015年4月18日 下午5:03:06 dasong.jds Exp $
 */
public interface TestTemplate {

    /**
     * 测试执行。
     * 
     * @param testCallback      测试具体内容
     */
    public <R, T> void execute(TestCallback<R, T> testCallback);

    /**
     * ccil方式实现用例。
     * 
     * @param caseExpr          用例表达式   - 描述阶段，支持caseId;caseDescription格式
     * @param ccilPrepare       准备ccil - 准备阶段
     * @param ccilExecute       执行ccil - 执行阶段
     * @param ccilCheck         检查ccil - 检查阶段
     * @param ccilClear         清除ccil - 清除阶段
     */
    public void executeByCcil(String caseExpr, String ccilPrepare, String ccilExecute,
                              String ccilCheck, String ccilClear);

}
