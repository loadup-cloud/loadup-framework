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
 * 测试用例实现的主体内容，包含用例描述、准备、执行、检查、清理等5个阶段。
 * 
 * @author dasong.jds
 * @version $Id: TestCallback.java, v 0.1 2015年4月18日 下午5:05:51 dasong.jds Exp $
 */
public interface TestCallback<R, T> {

    /**
     * 测试用例描述。
     * 
     * @return      描述信息
     */
    public String caseDescribe();

    /**
     * 测试用例准备阶段。主要包含：
     * <li>必要的mock；</li>
     * <li>必要的业务数据准备；</li>
     * <li>业务请求对象拼装；</li>
     * 
     * @param ctx           测试上下文
     * @return              准备完成的业务请求对象
     */
    public R prepare(TestContext ctx);

    /**
     * 测试用例执行阶段。
     * 
     * @param ctx           测试上下文
     * @param request       业务请求
     * @return              业务处理结果
     */
    public T execute(TestContext ctx, R request);

    /**
     * 测试用例检查阶段，主要包含：
     * <li>业务处理结果检查；</li>
     * <li>业务数据检查；<li>
     * <li>必要的日志输出检查；</li>
     * 
     * @param ctx           测试上下文
     * @param request       业务请求
     * @param result        业务处理结果
     */
    public void check(TestContext ctx, R request, T result);

    /**
     * 测试用例清理阶段，主要包含：
     * <li>mock内容重置；</li>
     * <li>清理生成的业务数据；</li>
     * 
     * @param ctx           测试上下文
     * @param request       业务请求
     * @param result        业务处理结果
     */
    public void clear(TestContext ctx, R request, T result);
}
