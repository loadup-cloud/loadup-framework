package com.alipay.test.acts.mock.model;

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

import java.util.List;

import com.alipay.test.acts.mock.processor.MockUtils;

/**
 * AtsSingleMock
 * 
 * 创建单个方法的mock，用起来较方便
 * 
 * @author midang
 * @version $Id: AtsSingleMock.java,v 0.1 2010-11-18 下午04:32:52 midang Exp $
 */
public class AtsSingleMock {
    private final String   container;
    private final String   beanName;
    private final Class<?> beanClass;
    private final String   methodName;

    /**
     * 构造
     * 
     * @param container 容器名
     * @param beanName  期望mock的bean名
     * @param beanClass 期望mock的bean的class
     * @param methodName 期望mock的方法名
     */
    public AtsSingleMock(String container, String beanName, Class<?> beanClass, String methodName) {
        this.container = container;
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.methodName = methodName;
    }

    /**
     * 添加mock
     * 
     * @param callback 回调，可为null
     * @return
     */
    public boolean addMock(MockCallBack callback) {
        return MockUtils.addMock(container, beanName, beanClass, methodName, callback);
    }

    /**
     * 录制返回值
     * 
     * @param returnObject 返回值
     * @return
     */
    public boolean recordMock(Object returnObject) {

        return MockUtils.recordMock(container, beanName, methodName, returnObject);
    }

    /**
     * 录制异常
     * 
     * @param e 异常
     * @return
     */
    public boolean recordMock(Throwable e) {
        return MockUtils.recordMock(container, beanName, methodName, e);
    }

    /**
     * 删除mock，恢复原始功能
     * 
     * @return
     */
    public boolean removeMock() {
        return MockUtils.removeMock(container, beanName, methodName);
    }

    /**
     * 检查mock入参
     * 
     * 只支持基本类型、枚举、正确重载了toString()的复杂对象，其他复杂应用请传入回调，自行处理
     * 
     * @param expArgs
     * @return
     */
    public boolean argsCheck(Object... expArgs) {

        return MockUtils.argsCheck(container, beanName, methodName, expArgs);
    }

    /**
     * 检查mock入参,接受参数列表，但必须按序
     * 
     * 只支持基本类型、枚举、正确重载了toString()的复杂对象，其他复杂应用请传入回调，自行处理
     * 
     * @param expArgs 接受参数列表，但必须按序
     * @return
     */
    public boolean argsCheck(List<Object[]> expArgs) {

        return MockUtils.argsCheck(container, beanName, methodName, expArgs);
    }

    /**
     * 获取mock入参检验失败的错误消息
     * 
     * @return
     */
    public String getArgsCheckErrMsg() {
        return MockUtils.getArgsCheckErrMsg(container, beanName, methodName);
    }

}
