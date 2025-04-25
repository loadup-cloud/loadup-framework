/**
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.github.loadup.components.testify.driver.listener;

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

import com.github.loadup.components.testify.driver.annotation.TCList;
import com.github.loadup.components.testify.driver.enums.SuiteFlag;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.testng.SkipException;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Sofa TesgNG 测试执行器TestNGTestActivator代理
 *
 * 
 *
 */
@Slf4j
public class TestNGTestActivatorProxyFactory implements MethodInterceptor {

    /**
     * 该类在内部同时作为代理拦截器使用
     */
    private static TestNGTestActivatorProxyFactory methodInterceptor;

    /**
     * 拦截器单例锁
     */
    private static byte[] singleInterceptorLock = new byte[0];

    /**
     * 被代理对象
     */
    private final Object targetObject;

    //方法缓存，主要避免重复比较方法，提高并发时测试的执行速度
    private final Map<Method, Method> methodMapper = new HashMap<Method, Method>();

    private TestNGTestActivatorProxyFactory(Object targetObject) {
        this.targetObject = targetObject;
    }

    /**
     * 获取TestNGTestActivator代理对象
     *
     * @param targetObject 要代理的TestNGTestActivator对象
     * @return 代理对象
     */
    public static Object getProxy(Object targetObject) {
        if (methodInterceptor == null) {
            synchronized (singleInterceptorLock) {
                if (methodInterceptor == null) {
                    methodInterceptor = new TestNGTestActivatorProxyFactory(targetObject);
                }
            }
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Object.class);
        enhancer.setCallback(methodInterceptor);
        Object delegate = enhancer.create();
        return delegate;
    }

    /**
     * @see net.sf.cglib.proxy.MethodInterceptor#intercept(Object, Method, Object[], net.sf.cglib.proxy.MethodProxy)
     */
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
            throws Throwable {

        //check testOnly规则，如果有配置TCList的testOnly，那么只运行testOnly中出现的caseId，其他直接fail以便追踪
        if (!checkTestOnly(method, args)) {
            return null;
        }

        if (!methodMapper.containsKey(method)) {
            Method m = findEqual(method, targetObject);
            if (m == null) {
                throw new RuntimeException("TestNGTestActivatorProxyFactory找不到对应的目标方法:"
                        + method.getName());
            }
            m.setAccessible(true);
            methodMapper.put(method, m);
        }
        Method equalMethod = methodMapper.get(method);

        Object result = equalMethod.invoke(targetObject, args);
        return result;

    }

    /**
     * check testOnly规则，如果有配置TCList的testOnly，那么只运行testOnly中出现的caseId，其他直接fail以便追踪
     *
     * @param method
     * @param args
     * @return
     */
    private boolean checkTestOnly(Method method, Object[] args) {
        //TestNGTestActivator.invokeTestNGTestMethod(String testClass, Method targetMethod, Object[] args)
        if (args.length == 3) {
            if (!(args[1] instanceof Method) || !(args[2] instanceof Object[])) {
                return true;
            }

            Method testMethod = (Method) args[1];
            Object[] testArgs = (Object[]) args[2];

            //仅正常测试方法需要判断testOnly标识
            if (testMethod.isAnnotationPresent(org.testng.annotations.Test.class)
                    && testMethod.isAnnotationPresent(TCList.class)) {
                String[] includeCaseIds = testMethod.getAnnotation(TCList.class).testOnly();

                //只有配置了TCList testOnly的时候才判断caseId是否出现在testOnly中
                if (includeCaseIds != null && includeCaseIds.length > 0) {
                    Set<String> includeCaseIdSet = new HashSet<String>();
                    for (String caseId : includeCaseIds) {
                        includeCaseIdSet.add(caseId);
                    }

                    //当测试方法参数个数大于0时，认为第一个参数为caseId
                    if (testArgs != null && testArgs.length > 0) {
                        Object currentCaseId = testArgs[0];
                        if (currentCaseId != null
                                && includeCaseIdSet.contains(currentCaseId.toString())) {
                            return true;
                        } else {
                            throw new SkipException("case [" + testArgs[0] + "] is not run.");
                        }
                    }
                } else {
                    if (testArgs != null && testArgs.length > 0) {
                        String suiteFlagCode = testArgs[testArgs.length - 1].toString();
                        if (SuiteFlag.getByCode(suiteFlagCode) == SuiteFlag.DELETE) {
                            TestifyLogUtil.warn(log, "case " + testArgs[0] + " 跳过测试");
                            throw new SkipException("case [" + testArgs[0] + "] is not run.");
                        } else
                            return true;
                    }
                }
            }

        }

        return true;
    }

    private Method findEqual(Method targetMethod, Object testCase) {
        Method equalMethod = null;
        List<Method> list = new ArrayList<Method>();
        getMethods(testCase.getClass(), list);
        for (Method m : list) {
            if (isMethodEqual(m, targetMethod)) {
                equalMethod = m;
                break;
            }
        }
        return equalMethod;
    }

    /**
     * 获取声明的方法，一直递归找到Object
     *
     * @return
     */
    private void getMethods(Class<?> cls, List<Method> list) {
        if (cls == null) {
            return;
        }
        list.addAll(Arrays.asList(cls.getDeclaredMethods()));
        getMethods(cls.getSuperclass(), list);
    }

    private boolean isMethodEqual(Method mtd1, Method mtd2) {
        if (mtd1 != null && mtd2 != null) {
            if (mtd1.getName().equals(mtd2.getName())) {
                if (!mtd1.getReturnType().getName().equals(mtd2.getReturnType().getName()))
                    return false;
                Class<?>[] params1 = mtd1.getParameterTypes();
                Class<?>[] params2 = mtd2.getParameterTypes();
                if (params1.length == params2.length) {
                    for (int i = 0; i < params1.length; i++) {
                        if (!params1[i].getName().equals(params2[i].getName()))
                            return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
