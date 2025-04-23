/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
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
import com.alipay.test.acts.model.VirtualEventSet;

import java.util.List;

/**
 * 带消息检查的CallBack
 *
 * @author fenglei.fl
 * @version $Id: PrepareCallBackWithEvent.java, v 0.1 2015年1月12日 下午12:17:48
 * fenglei.fl Exp $
 */
public class PrepareCallBackWithEvent implements PrepareCallBack {
    // 事件校验
    public VirtualEventSet prepareExpectEventSet(VirtualArgs args, VirtualDataSet depDataSet,
                                                 Object expectResult) {
        return null;
    }

    @Override
    public VirtualDataSet prepareDepDataSet() {
        return null;
    }

    @Override
    public VirtualArgs prepareArgs(VirtualDataSet depDataSet) {
        return null;
    }

    @Override
    public VirtualDataSet prepareExpectDataSet(VirtualArgs args, VirtualDataSet depDataSet) {
        return null;
    }

    @Override
    public Object prepareExpectResult(VirtualArgs args, VirtualDataSet depDataSet) {
        return null;
    }

    @Override
    public VirtualArgs prepareExpectInvokeOutArgs() {
        return null;
    }


    public List<String> prepareComponent() {
        return null;
    }

    public List<String> executeComponent() {
        return null;
    }

    public List<String> checkComponent() {
        return null;
    }

    public List<String> clearComponent() {
        return null;
    }

}
