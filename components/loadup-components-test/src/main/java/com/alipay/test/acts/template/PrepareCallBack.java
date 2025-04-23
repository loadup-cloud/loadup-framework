package com.alipay.test.acts.template;

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

import com.alipay.test.acts.model.VirtualArgs;
import com.alipay.test.acts.model.VirtualDataSet;

public interface PrepareCallBack {
    // 前置的db数据准备
    public VirtualDataSet prepareDepDataSet();

    // 入参准备
    public VirtualArgs prepareArgs(VirtualDataSet depDataSet);

    // 预期的db数据准备
    public VirtualDataSet prepareExpectDataSet(VirtualArgs args, VirtualDataSet depDataSet);

    // 预期结果准备
    public Object prepareExpectResult(VirtualArgs args, VirtualDataSet depDataSet);

    // 预期所难的服务，若有依赖其它系统，拦截验证调用外围服务的参数准备-jvm模式下使用
    public VirtualArgs prepareExpectInvokeOutArgs();

    // mock对象准备-jvm模式下使用
//    public VirtualMockSet prepareMockResult();

}
