/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.alipay.test.acts.driver.listener;

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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;


/**
 * TestNG测试方法执行前监听器，目前用于完成TestNGTestActivator的代理
 * 
 * @author muxiao.jl
 * @version $Id: TestNGBeforeTestConfig.java, v 0.1 2014年8月6日 下午3:51:26 muxiao.jl Exp $
 */
public class TestNGBeforeTestConfig extends TestListenerAdapter {

    /** TestNGTestActivator上一次代理对象存储 */
    private static Object lastDelegatedTestActivator = null;

    /** 
     * @see TestListenerAdapter#onTestStart(ITestResult)
     */
    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result);
        delegateTestNGTestActivator();
    }

    private static byte[] delegateLock = new byte[0];

    private void delegateTestNGTestActivator() {
        synchronized (delegateLock) {
            try {
                //设置OsgiTestMethodProxy testNGService字段为代理类
//                Field testNgServiceField = OsgiTestMethodProxy.class
//                    .getDeclaredField("testNGService");
//                testNgServiceField.setAccessible(true);
//                Object oriTestNGService = testNgServiceField.get(null);
//                if (oriTestNGService == lastDelegatedTestActivator) {
//                    oriTestNGService已经是代理过的对象则不用再代理
//                    return;
//                }
//                Object delegatedTestNGService = TestNGTestActivatorProxyFactory
//                    .getProxy(oriTestNGService);
//                testNgServiceField.set(null, delegatedTestNGService);
//                lastDelegatedTestActivator = delegatedTestNGService;

                //设置OsgiTestMethodProxy serviceTrigger字段为代理类
//                Field serviceTriggerField = OsgiTestMethodProxy.class
//                    .getDeclaredField("serviceTrigger");
//                serviceTriggerField.setAccessible(true);
//                Object delegatedServiceTrigger = delegatedTestNGService.getClass()
//                    .getDeclaredMethod("invokeTestNGTestMethod", String.class, Method.class,
//                        Object[].class);
//                serviceTriggerField.set(null, delegatedServiceTrigger);

                System.out.println("delegate TestNGTestActivator finished.");
            } catch (Throwable e) {
                //代理失败不抛出异常，不影响测试主流程
                System.out.println("delegate TestNGTestActivator failed!");
                e.printStackTrace();
            }
        }
    }
}
