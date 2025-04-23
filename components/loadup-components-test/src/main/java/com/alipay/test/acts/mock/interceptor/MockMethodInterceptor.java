package com.alipay.test.acts.mock.interceptor;

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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.aop.framework.Advised;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.mock.enums.MockStatus;
import com.alipay.test.acts.mock.model.MockCallBack;
import com.alipay.test.acts.mock.model.MockObject;
import com.alipay.test.acts.util.BeanUtil;

/**
 * AtsMockMethodInterceptor
 * 
 * 支持接口动态代理和类的动态代理
 * 
 * @author midang
 * @version $Id: MockMethodInterceptor.java,v 0.1 2010-11-16 下午03:03:26 midang Exp $
 */
public class MockMethodInterceptor implements MethodInterceptor, InvocationHandler {

    private Object                            orgBean;
    private Object                            aopBean;
    private boolean                           isAdvised       = false;
    private final Map<Method, List<Object[]>> invokedArgs     = new HashMap<Method, List<Object[]>>();
    private final Map<String, MockObject>     results         = new HashMap<String, MockObject>();
    private final List<String>                mockMethodNames = new ArrayList<String>();
    private final Map<String, MockCallBack>   callbacks       = new HashMap<String, MockCallBack>();

    public MockMethodInterceptor(Object orgBean) {
        // 已经被spring aop使用代理过的
        if (orgBean instanceof Advised) {
            isAdvised = true;
            aopBean = orgBean;
            this.orgBean = BeanUtil.getTargetBean(orgBean);
            return;
        }
        this.orgBean = orgBean;
    }

    public List<Object[]> getInovkeArgs(String methodName) {
        for (Map.Entry<Method, List<Object[]>> entry : invokedArgs.entrySet()) {
            if (StringUtils.equals(entry.getKey().getName(), methodName)) {
                invokedArgs.remove(entry.getKey());
                return entry.getValue();
            }
        }
        return null;
    }

    public Object[] getInovkeArg(String methodName, int nIndex) {
        for (Map.Entry<Method, List<Object[]>> entry : invokedArgs.entrySet()) {
            if (StringUtils.equals(entry.getKey().getName(), methodName)) {
                List<Object[]> args = entry.getValue();
                if (args == null || args.size() <= nIndex)
                    return null;
                Object[] ret = args.get(nIndex);
                args.remove(nIndex);
                return ret;
            }
        }
        return null;
    }

    public void addMock(String methodName, MockCallBack callback) {
        // 不重复
        if (!mockMethodNames.contains(methodName))
            mockMethodNames.add(methodName);

        // 清空该method对应的参数和返回值
        for (Map.Entry<Method, List<Object[]>> entry : invokedArgs.entrySet()) {
            if (StringUtils.equals(entry.getKey().getName(), methodName)) {
                invokedArgs.remove(entry.getKey());
                entry.getValue().clear();
                break;
            }
        }
        results.remove(methodName);
        if (callback != null)
            callbacks.put(methodName, callback);
        else
            callbacks.remove(methodName);
    }

    public void addMock(String methodName) {
        // 不重复
        if (!mockMethodNames.contains(methodName))
            mockMethodNames.add(methodName);

        // 清空该method对应的参数和返回值
        for (Map.Entry<Method, List<Object[]>> entry : invokedArgs.entrySet()) {
            if (StringUtils.equals(entry.getKey().getName(), methodName)) {
                invokedArgs.remove(entry.getKey());
                entry.getValue().clear();
                break;
            }
        }
        results.remove(methodName);

    }

    public MockStatus removeMock(String methodName) {
        mockMethodNames.remove(methodName);
        if (mockMethodNames.isEmpty()) {
            return MockStatus.CAN_REMOVE;
        }
        return MockStatus.MOCKED;
    }

    public MockStatus removeAllMock() {
        mockMethodNames.clear();
        return MockStatus.CAN_REMOVE;
    }

    public void recordMock(String methodName, Object returnObject, Throwable e) {
        MockObject mockObj = new MockObject();
        if (e != null) {
            mockObj.setThrowable(true);
            mockObj.setThrowable(e);
        } else {
            mockObj.setReturnObject(returnObject);
        }
        results.put(methodName, mockObj);
    }

    /**
     * 不同于getInovkeArgs,本方法不会删除已有的参数列表
     * @param methodName
     * @return 参数列表的list
     */
    public List<Object[]> peekInovkeArgs(String methodName) {
        for (Map.Entry<Method, List<Object[]>> entry : invokedArgs.entrySet()) {
            if (StringUtils.equals(entry.getKey().getName(), methodName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean isNeedMock(Method method) {
        return mockMethodNames.contains(method.getName());
    }

    private synchronized void saveInvokedArgs(Method method, Object[] args) {
        if (invokedArgs.containsKey(method)) {
            List<Object[]> storedArgs = invokedArgs.get(method);
            storedArgs.add(args);
        } else {
            List<Object[]> storedArgs = new ArrayList<Object[]>();
            storedArgs.add(args);
            invokedArgs.put(method, storedArgs);
        }
    }

    private Object getResult(Method method) throws Throwable {
        MockObject mockObj = results.get(method.getName());
        if (mockObj == null)
            return null;
        if (mockObj.isThrowable())
            throw mockObj.getThrowable();
        return mockObj.getReturnObject();
    }

    private MockCallBack getCallBack(Method method) {
        return callbacks.get(method.getName());
    }

    /**
     * 类的拦截
     * 
     * @param obj
     * @param method
     * @param args
     * @param proxy
     * @return
     * @throws Throwable
     * @see net.sf.cglib.proxy.MethodInterceptor#intercept(Object, Method, Object[], net.sf.cglib.proxy.MethodProxy)
     */
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                                                                                        throws Throwable {
        if (!isNeedMock(method)) {
            return isAdvised ? proxy.invoke(aopBean, args) : proxy.invokeSuper(obj, args);
        }

        try {
            saveInvokedArgs(method, args);
            MockCallBack callback = getCallBack(method);
            if (callback != null) {
                Method m = callback.getClass().getMethod(method.getName(),
                    method.getParameterTypes());
                m.setAccessible(true);
                return m.invoke(callback, args);
            }
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
        return getResult(method);

    }

    /**
     * 接口的拦截
     * 
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     * @see InvocationHandler#invoke(Object, Method, Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        try {
            if (!isNeedMock(method)) {
                return isAdvised ? method.invoke(aopBean, args) : method.invoke(orgBean, args);
            }
            saveInvokedArgs(method, args);
            MockCallBack callback = getCallBack(method);
            if (callback != null) {
                Method m = callback.getClass().getMethod(method.getName(),
                    method.getParameterTypes());
                m.setAccessible(true);
                return m.invoke(callback, args);
            }
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
        return getResult(method);
    }

}
