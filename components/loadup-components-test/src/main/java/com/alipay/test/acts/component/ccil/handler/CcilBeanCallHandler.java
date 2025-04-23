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

import java.lang.reflect.Method;

import javassist.Modifier;

import org.springframework.util.ReflectionUtils;

import com.alipay.test.acts.component.CCComponent;
import com.alipay.test.acts.component.InvokeContext;
import com.alipay.test.acts.component.ccil.CcilBeanCall;
import com.alipay.test.acts.component.ccil.CcilExpr;
import com.alipay.test.acts.component.impl.CCComponentImpl;
import com.alipay.test.acts.exception.ActsException;
import com.alipay.test.acts.manage.TestLogger;

/**
 * bean调用处理器。
 * 
 * @author dasong.jds
 * @version $Id: CcilBeanCallHandler.java, v 0.1 2015年7月21日 下午10:38:11 dasong.jds Exp $
 */
public class CcilBeanCallHandler implements CcilExprHandler {

    /** 
     * @see com.ipay.itest.common.repository.ccil.CcilExprHandler#accept(com.ipay.itest.common.ccil.CcilExpr)
     */
    @Override
    public boolean accept(CcilExpr expr) {
        return expr instanceof CcilBeanCall;
    }

    /** 
     * @see com.ipay.itest.common.repository.ccil.CcilExprHandler#handle(com.ipay.itest.common.repository.InvokeContext, com.ipay.itest.common.ccil.CcilExpr)
     */
    @Override
    public Object handle(InvokeContext invokeContext, CcilExpr expr) {

        CcilBeanCall beanCall = (CcilBeanCall) expr;

        invokeContext.push();
        try {

            new CcilInvokeParamHandler().handleInvokeParams(invokeContext, beanCall.getParams());

            CCComponent com = createComponent(invokeContext, beanCall);

            Object result = com.invoke(invokeContext);

            TestLogger.getLogger().info(buildInvokeLog(expr, result));

            return result;
        } finally {
            invokeContext.pop();
        }

    }

    /**
     * 动态创建组件。
     * 
     * @param invokeContext
     * @param beanCall
     * @return
     */
    private CCComponent createComponent(InvokeContext invokeContext, CcilBeanCall beanCall) {

        String id = beanCall.getBeanName() + "_" + beanCall.getMethodName();
        if (beanCall.getBundle() != null) {
            id = id + "$" + beanCall.getBundle().replaceAll("\\.", "_");
        }

        Object bean = invokeContext.getTestContext().getBean(beanCall.getBeanName(),
            beanCall.getBundle());

        if (bean == null) {
            throw new ActsException("can't find bean[name=" + beanCall.getBeanName() + ",bundle="
                                    + beanCall.getBundle() + "]");
        }

        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());

        Method method = null;
        for (Method m : methods) {
            if (Modifier.isPublic(m.getModifiers()) && m.getName().equals(beanCall.getMethodName())) {
                method = m;
                break;
            }
        }
        if (method == null) {
            throw new ActsException("can't find method[name=" + beanCall.getMethodName()
                                    + ",class=" + bean.getClass() + "]");
        }

        CCComponent com = new CCComponentImpl(id, bean, method);
        return com;
    }

    /**
     * 构建调用信息。
     * 
     * @param ccil    
     * @param result     
     */
    private String buildInvokeLog(CcilExpr expr, Object result) {
        StringBuilder msg = new StringBuilder("ccil beanCall handle[ccil=");
        msg.append(expr);
        msg.append(",result=");
        msg.append(result);
        msg.append("]");
        return msg.toString();
    }

}
