package com.alipay.test.acts.utils;

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

import com.alipay.test.acts.mock.model.AtsSingleMock;
import com.alipay.test.acts.mock.model.MockCallBack;
import com.alipay.test.acts.model.VirtualObject;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InterfaceMaker;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class MockUtil {
//    public static AtsSingleMock doMock(VirtualMock virtualMock, MethodInterceptor methodInterceptor) {
//        return fetchAtsSingleMock(virtualMock.getContainer(), virtualMock.getBeanName(),
//            virtualMock.getBeanClass(), virtualMock.getMethodName(), virtualMock.getMockResult(),
//            virtualMock,methodInterceptor);
//
//    }
//
//    public static AtsSingleMock fetchAtsSingleMock(String container, String beanName,
//                                                   String beanClass, String methodName,
//                                                   final VirtualObject mockResult,
//                                                   final VirtualMock virtualMock,MethodInterceptor methodInterceptor) {
//        AtsSingleMock atsSingleMock = null;
//        try {
//            Class<?> beanClazz = Class.forName(beanClass);
//            atsSingleMock = new AtsSingleMock(container, beanName, beanClazz, methodName);
//
//            InterfaceMaker im = new InterfaceMaker();
//            im.setClassLoader(MockUtil.class.getClassLoader());
//            Method[] methods = beanClazz.getMethods();
//            for (Method method : methods) {
//                if (method.getName().equals(methodName)) {
//                    im.add(method);
//                }
//            }
//            Class<?> myInterface = im.create();
//
//            Enhancer enhancer = new Enhancer();
//            enhancer.setClassLoader(MockUtil.class.getClassLoader());
//            enhancer.setSuperclass(MockCallBack.class);
//            enhancer.setInterfaces(new Class[] { myInterface, MockCallBack.class });
//
//            if(null!=methodInterceptor){
//
//                //支持自定义拦截操作
//                enhancer.setCallback(methodInterceptor);
//            }else {
//                enhancer.setCallback(new MethodInterceptor() {
//
//                    @Override
//                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
//                            throws Throwable {
//                        virtualMock.setMockArgs(args);
//                        if (null == mockResult.objClass || mockResult.objClass.equals("null")) {
//                            return null;
//                        } else if (mockResult.object instanceof Exception) {
//                            //throw new RuntimeException((Exception) mockResult.object);
//                            throw (Exception) mockResult.object;
//                        } else {
//
//                            return mockResult.getObject();
//
//                        }
//                    }
//                });
//            }
//            atsSingleMock.addMock((MockCallBack) enhancer.create());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//        return atsSingleMock;
//    }
}
