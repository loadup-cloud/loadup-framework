/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.impl.mock;

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

import com.alipay.test.acts.component.CCComponent;
import com.alipay.test.acts.component.InvokeContext;
import com.alipay.test.acts.exception.ActsException;
import com.alipay.test.acts.manage.TestProfiler;
import com.alipay.test.acts.support.TestAction;
import com.alipay.test.acts.support.TestContext;
import com.alipay.test.acts.support.enums.ActionType;

/**
 * mock组件实现。
 * 
 * @author dasong.jds
 * @version $Id: MockComponentImpl.java, v 0.1 2015年5月30日 下午10:32:39 dasong.jds Exp $
 */
public class MockComponentImpl implements CCComponent {

    /** 组件ID */
    private final String id;

    /** mock阶段组件 */
    private CCComponent  mock;

    /** 校验阶段组件 */
    private CCComponent  verify;

    /** 清理阶段组件 */
    private CCComponent  unmock;

    /**
     * 构造mock组件。
     * 
     * @param id
     */
    public MockComponentImpl(String id) {
        super();
        this.id = id;
    }

    /** 
     * @see com.CCComponent.itest.common.repository.Component#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /** 
     * @see com.CCComponent.itest.common.repository.Component#invoke(com.ipay.itest.common.repository.InvokeContext)
     */
    @Override
    public Object invoke(InvokeContext invokeContext) {

        if (mock == null) {
            throw new ActsException("mock define error, no 'mock' phase[id=" + getId() + "]");
        }

        TestProfiler.enter("[setup mock]" + getId());
        try {
            Object result = mock.invoke(invokeContext);

            final InvokeContext clone = invokeContext.cloneWithVariables();

            registerVerifyAction(invokeContext, clone);

            registerClearAction(invokeContext, clone);

            return result;
        } finally {
            TestProfiler.release();
        }
    }

    /**
     * 
     * @param invokeContext
     * @param clone
     */
    private void registerClearAction(InvokeContext invokeContext, final InvokeContext clone) {
        if (unmock != null) {
            invokeContext.getTestContext().getActionManager()
                .registerAction(ActionType.CLEAR, new TestAction() {

                    @Override
                    public void doAction(TestContext testContext) {
                        TestProfiler.enter("[clear mock]" + getId());
                        try {

                            unmock.invoke(clone);
                        } finally {
                            TestProfiler.release();
                        }
                    }
                });
        }
    }

    /**
     * 
     * @param invokeContext
     * @param clone
     */
    private void registerVerifyAction(InvokeContext invokeContext, final InvokeContext clone) {
        if (verify != null) {
            invokeContext.getTestContext().getActionManager()
                .registerAction(ActionType.CHECK, new TestAction() {

                    @Override
                    public void doAction(TestContext testContext) {
                        TestProfiler.enter("[verify mock]" + getId());
                        try {
                            verify.invoke(clone);
                        } finally {
                            TestProfiler.release();
                        }
                    }
                });
        }
    }

    /**
     * Setter method for property <tt>mock</tt>.
     * 
     * @param mock value to be assigned to property mock
     */
    public void setMock(CCComponent mock) {
        this.mock = mock;
    }

    /**
     * Setter method for property <tt>verify</tt>.
     * 
     * @param verify value to be assigned to property verify
     */
    public void setVerify(CCComponent verify) {
        this.verify = verify;
    }

    /**
     * Setter method for property <tt>unmock</tt>.
     * 
     * @param unmock value to be assigned to property unmock
     */
    public void setUnmock(CCComponent unmock) {
        this.unmock = unmock;
    }

}
