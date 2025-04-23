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

import java.lang.reflect.Method;

import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;


/**
 * Acts mock扩展使用工具类
 * 
 * @author xiaoleicxl
 * @version $Id: ActsMockUtil.java, v 0.1 2016年7月29日 上午11:56:32 xiaoleicxl Exp $
 */
public class ActsMockUtil {

    /**
     * 当执行容器container中的beanClass服务的methodName方法时，执行mock
     * 当入参是mockArgs时返回mockResult
     * 
     * @param container 容器名称 格式"bundleName;containerBeanId"
     * @param beanName 被mock服务的beanId
     * @param beanClass 被mock服务的类全称
     * @param methodName 被mock方法名称
     * @param mockResult
     * @param argMatchers 入参匹配器，不需要匹配的入参用AnyRequestMatcher填充。
     * @param argumentCounts 入参个数，目前最多支持5个入参
     */
    @SuppressWarnings("unchecked")
    public static void mockDiffResultByDiffRequest(String container, String beanName,
                                                   String beanClass, String methodName,
                                                   Object mockResult,
                                                   ArgumentMatcher[] argMatchers, int argumentCounts) {

        //获取容器
//        Object containerBean = BeanUtils.getBean(container);
//        containerBean = BeanUtils.getTargetBean(containerBean);

        try {

            //获取被mock服务类
            Class<?> beanClazz = Class.forName(beanClass);

            //获取被mock方法
            Method[] methods = beanClazz.getMethods();
            Method mockMethod = null;

            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    mockMethod = method;
                }
            }
            //装载mock
            Object mockService =null;// MockitoUtil.in(containerBean).mock(beanClazz);

            if (1 == argumentCounts) {
                Mockito.when(mockMethod.invoke(mockService, Mockito.argThat(argMatchers[0])))
                    .thenReturn(mockResult);
            } else if (2 == argumentCounts) {
                Mockito.when(
                    mockMethod.invoke(mockService, Mockito.argThat(argMatchers[0]),
                        Mockito.argThat(argMatchers[1]))).thenReturn(mockResult);
            } else if (3 == argumentCounts) {
                Mockito.when(
                    mockMethod.invoke(mockService, Mockito.argThat(argMatchers[0]),
                        Mockito.argThat(argMatchers[1]), Mockito.argThat(argMatchers[2])))
                    .thenReturn(mockResult);
            } else if (4 == argumentCounts) {
                Mockito.when(
                    mockMethod.invoke(mockService, Mockito.argThat(argMatchers[0]),
                        Mockito.argThat(argMatchers[1]), Mockito.argThat(argMatchers[2]),
                        Mockito.argThat(argMatchers[3]))).thenReturn(mockResult);
            } else if (5 == argumentCounts) {
                Mockito.when(
                    mockMethod.invoke(mockService, Mockito.argThat(argMatchers[0]),
                        Mockito.argThat(argMatchers[1]), Mockito.argThat(argMatchers[2]),
                        Mockito.argThat(argMatchers[3]), Mockito.argThat(argMatchers[4])))
                    .thenReturn(mockResult);
            } else {
                throw new RuntimeException("不支持超过5个参数的自定义mock！", new Exception());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("自定义mock异常！", e);
        }
    }

    /**
     * 卸载指定容器的mock，配合上述编码扩展mock使用
     * @param container
     */
    public static void unMock(String container) {
//
//        //获取容器
//        Object containerBean = BeanUtils.getBean(container);
//        containerBean = BeanUtils.getTargetBean(containerBean);
//
//        //卸载mock
//        MockitoUtil.in(containerBean).unmock(true);

    }

}
