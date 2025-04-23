package com.alipay.test.acts.mock.processor;

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

import com.alipay.test.acts.mock.enums.MockStatus;
import com.alipay.test.acts.mock.interceptor.MockMethodInterceptor;
import com.alipay.test.acts.mock.model.MockCallBack;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mock代理，支持接口动态代理和类的动态代理
 *
 * @author midang
 *
 * @version $Id: MockProxy.java,v 0.1 2010-11-16 下午14:27:45 midang Exp $
 */
public class MockProxy {

    private static Map<String, MockMethodInterceptor> mockStore = new HashMap<String, MockMethodInterceptor>();

    @SuppressWarnings("rawtypes")
    public static Object getProxy(Object containerBean, Object targetBean, Class targetClass) {

        MockMethodInterceptor interceptor = new MockMethodInterceptor(targetBean);

        saveMock(containerBean, targetBean, interceptor);

        if (targetClass.isInterface()) {
            return Proxy.newProxyInstance(targetClass.getClassLoader(),
                new Class[] { targetClass }, interceptor);
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(interceptor);
        return enhancer.create();

    }

    public static List<Object[]> getInovkeArgs(Object containerBean, Object targetBean,
                                               String methodName) {
        MockMethodInterceptor interceptor = mockStore.get(genKey(containerBean, targetBean));
        if (interceptor == null)
            return null;
        return interceptor.getInovkeArgs(methodName);
    }

    public static Object[] getInovkeArg(Object containerBean, Object targetBean, String methodName,
                                        int nIndex) {
        MockMethodInterceptor interceptor = mockStore.get(genKey(containerBean, targetBean));
        if (interceptor == null)
            return null;
        return interceptor.getInovkeArg(methodName, nIndex);
    }

    public static void recordMock(Object containerBean, Object targetBean, String methodName,
                                  Object returnObject, Throwable e) {
        MockMethodInterceptor interceptor = mockStore.get(genKey(containerBean, targetBean));
        if (interceptor == null)
            return;
        interceptor.recordMock(methodName, returnObject, e);
    }

    public static void addMock(Object containerBean, Object targetBean, String methodName) {
        MockMethodInterceptor interceptor = mockStore.get(genKey(containerBean, targetBean));
        if (interceptor == null)
            return;
        interceptor.addMock(methodName);
    }

    public static void addMock(Object containerBean, Object targetBean, String methodName,
                               MockCallBack callback) {
        MockMethodInterceptor interceptor = mockStore.get(genKey(containerBean, targetBean));
        if (interceptor == null)
            return;
        interceptor.addMock(methodName, callback);
    }

    public static MockStatus removeMock(Object containerBean, Object targetBean, String methodName) {
        MockMethodInterceptor interceptor = mockStore.get(genKey(containerBean, targetBean));
        if (interceptor == null)
            return MockStatus.NONEMOCK;
        return interceptor.removeMock(methodName);
    }

    public static MockStatus removeAllMock(Object containerBean, Object targetBean) {
        MockMethodInterceptor interceptor = mockStore.get(genKey(containerBean, targetBean));
        if (interceptor == null)
            return MockStatus.NONEMOCK;
        return interceptor.removeAllMock();
    }

    public static List<Object[]> peekInovkeArgs(Object containerBean, Object targetBean,
                                                String methodName) {
        MockMethodInterceptor interceptor = mockStore.get(genKey(containerBean, targetBean));
        if (interceptor == null)
            return null;
        return interceptor.peekInovkeArgs(methodName);
    }

    private static void saveMock(Object containerBean, Object targetBean,
                                 MockMethodInterceptor interceptor) {

        mockStore.put(genKey(containerBean, targetBean), interceptor);
    }

    private static String genKey(Object containerBean, Object targetBean) {
        return containerBean.toString() + targetBean.hashCode();
    }

}
