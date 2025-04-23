/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.ccil.handler;

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
import com.alipay.test.acts.component.ccil.CcilExpr;
import com.alipay.test.acts.component.ccil.CcilInvoke;
import com.alipay.test.acts.exception.ActsException;
import com.alipay.test.acts.manage.TestLogger;
import com.alipay.test.acts.manage.TestRecorder;

/**
 * 组件调用处理器。
 * 
 * @author dasong.jds
 * @version $Id: CcilInvokeExecutor.java, v 0.1 2015年5月26日 下午10:38:14 dasong.jds Exp $
 */
public class CcilInvokeHandler implements CcilExprHandler {

    /** 
     * @see com.ipay.itest.common.repository.ccil.CcilExprHandler#accept(com.ipay.itest.common.ccil.CcilExpr)
     */
    @Override
    public boolean accept(CcilExpr expr) {
        return expr instanceof CcilInvoke;
    }

    /** 
     * @see com.ipay.itest.common.repository.ccil.CcilExprHandler#handle(com.ipay.itest.common.repository.InvokeContext, com.ipay.itest.common.ccil.CcilExpr)
     */
    @Override
    public Object handle(InvokeContext invokeContext, CcilExpr expr) {

        CcilInvoke invoke = (CcilInvoke) expr;

        invokeContext.push();
        try {

            new CcilInvokeParamHandler().handleInvokeParams(invokeContext, invoke.getParams());

            CCComponent com = invokeContext.getRefInvoker().getComponent(
                invoke.getRepositoryType(), invoke.getComponentId());

            if (com == null) {
                throw new ActsException("can't find component[id=" + invoke.getComponentId() + "]");
            }

            TestRecorder.useComponent(invoke.getRepositoryType(), invoke.getComponentId());

            Object result = com.invoke(invokeContext);

            TestLogger.getLogger().info(buildInvokeLog(expr, result));

            return result;
        } finally {
            invokeContext.pop();
        }
    }

    // ~~~ 私有方法

    /**
     * 构建调用信息。
     * 
     * @param ccil    
     * @param result     
     */
    private String buildInvokeLog(CcilExpr expr, Object result) {
        StringBuilder msg = new StringBuilder("ccil invoke handle[ccil=");
        msg.append(expr);
        msg.append(",result=");
        msg.append(result);
        msg.append("]");
        return msg.toString();
    }

}
