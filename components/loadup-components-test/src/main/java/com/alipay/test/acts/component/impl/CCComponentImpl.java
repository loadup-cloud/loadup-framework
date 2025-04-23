/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.impl;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.alipay.test.acts.component.CCInvoker;
import com.alipay.test.acts.component.InvokeContext;
import com.alipay.test.acts.component.annotation.CCParam;
import com.alipay.test.acts.exception.ActsException;
import com.alipay.test.acts.manage.TestLogger;
import com.alipay.test.acts.support.TestContext;
import com.alipay.test.acts.util.JavassistUtil;

/**
 * 组件实现。
 * 
 * <p>实现简单的组件调用。
 * 
 * @author dasong.jds
 * @version $Id: CCComponentImpl.java, v 0.1 2015年5月3日 下午11:02:57 dasong.jds Exp $
 */
public class CCComponentImpl extends AbstractComponent {

    /** 宿主对象 */
    private final Object host;

    /** 组件方法 */
    private final Method method;

    /**
     * 构造简单组件。
     * 
     * @param id        组件ID
     * @param host      组件宿主对象
     * @param method    组件方法
     */
    public CCComponentImpl(String id, Object host, Method method) {
        super(id);
        this.host = host;
        this.method = method;
    }

    /** 
     * @see com.CCComponent.itest.common.repository.Component#invoke(com.ipay.itest.common.repository.InvokeContext)
     */
    @Override
    public Object invoke(InvokeContext invokeContext) {

        Object[] arguments = getInvokeArgs(invokeContext);

        try {

            return this.method.invoke(host, arguments);

        } catch (InvocationTargetException e) {
            // error 继续抛，多为AssertError
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }

            StringBuilder msg = buildExceptionMsg(arguments);

            TestLogger.getLogger().error(e, msg);

            throw new ActsException(msg.toString(), e.getCause());

        } catch (Exception e) {
            StringBuilder msg = buildExceptionMsg(arguments);

            TestLogger.getLogger().error(e, msg);

            throw new ActsException(msg.toString(), e);
        }
    }

    /**
     * 获取调用参数。
     * 
     * @param invokeContext
     * @return
     */
    private Object[] getInvokeArgs(InvokeContext invokeContext) {

        Class<?>[] paramTypes = method.getParameterTypes();

        String[] paramNames = null;

        Object[] arguments = new Object[paramTypes.length];

        for (int i = 0; i < arguments.length; i++) {
            if (TestContext.class.isAssignableFrom(paramTypes[i])) {
                arguments[i] = invokeContext.getTestContext();
            } else if (CCInvoker.class.isAssignableFrom(paramTypes[i])) {
                arguments[i] = invokeContext.getRefInvoker();
            } else {
                // 尽可能晚装载
                if (paramNames == null) {
                    paramNames = getMethodParamNames();
                }
                arguments[i] = invokeContext.getAvailableVariable(paramNames[i], paramTypes[i]);
            }
        }
        return arguments;
    }

    // ~~~ 私有方法

    /**
     * 获取方法参数名。
     * <p>
     * 优先注解中取，其次从字节码中获取。
     * 
     * @return
     */
    private String[] getMethodParamNames() {

        String[] paramNames = new String[method.getParameterTypes().length];

        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        String[] paramNamesBC = null;

        for (int i = 0; i < paramNames.length; i++) {

            for (Annotation pa : paramAnnotations[i]) {
                if (pa instanceof CCParam) {
                    paramNames[i] = ((CCParam) pa).value();
                }
            }

            if (paramNames[i] == null) {
                // 尽可能晚装载
                if (paramNamesBC == null) {
                    paramNamesBC = JavassistUtil.getMethodParamNames(method.getDeclaringClass(),
                        method.getName());
                }
                paramNames[i] = paramNamesBC[i];
            }

            if (paramNames[i] == null) {
                TestLogger.getLogger().warn("component define error, can't find param's name[id=",
                    getId(), ",index=", i, "]");
            }
        }

        return paramNames;
    }

    /**
     * 构造异常信息。
     * 
     * @param args      参数
     * @return          异常信息
     */
    private StringBuilder buildExceptionMsg(Object... args) {
        StringBuilder msg = new StringBuilder("cc invoke error[id=");
        msg.append(getId());
        msg.append(",args=[");
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (i != 0) {
                    msg.append(";");
                }
                msg.append(args[i]);
            }
        } else {
            msg.append(args);
        }
        msg.append("]]");
        return msg;
    }

}
