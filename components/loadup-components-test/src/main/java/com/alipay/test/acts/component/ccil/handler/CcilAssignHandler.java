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

import com.alipay.test.acts.component.InvokeContext;
import com.alipay.test.acts.component.ccil.CcilAssign;
import com.alipay.test.acts.component.ccil.CcilExpr;
import com.alipay.test.acts.manage.TestLogger;

/**
 * 赋值语句处理器。
 * 
 * @author dasong.jds
 * @version $Id: CcilAssignHandler.java, v 0.1 2015年5月27日 下午12:30:49 dasong.jds Exp $
 */
public class CcilAssignHandler implements CcilExprHandler {

    /** 
     * @see com.ipay.itest.common.repository.ccil.CcilExprHandler#accept(com.ipay.itest.common.ccil.CcilExpr)
     */
    @Override
    public boolean accept(CcilExpr expr) {
        return expr instanceof CcilAssign;
    }

    /** 
     * @see com.ipay.itest.common.repository.ccil.CcilExprHandler#handle(com.ipay.itest.common.repository.InvokeContext, com.ipay.itest.common.ccil.CcilExpr)
     */
    @Override
    public Object handle(InvokeContext invokeContext, CcilExpr expr) {

        CcilAssign assignExpr = (CcilAssign) expr;

        String resultName = assignExpr.getName();

        Object result;
        if (assignExpr.getCcilInvoke() != null) {
            result = new CcilInvokeHandler().handle(invokeContext, assignExpr.getCcilInvoke());
        } else {
            result = new CcilBeanCallHandler().handle(invokeContext, assignExpr.getBeanCall());
        }

        invokeContext.setVariable2Parent(resultName, result);

        TestLogger.getLogger().info(buildHandleLog(assignExpr, result));

        return result;
    }

    /**
     * 构建处理信息。
     * 
     * @param ccil    
     * @param result     
     */
    private String buildHandleLog(CcilExpr expr, Object result) {
        StringBuilder msg = new StringBuilder("ccil assign handle[ccil=");
        msg.append(expr);
        msg.append(",result=");
        msg.append(result);
        msg.append("]");
        return msg.toString();
    }

}
