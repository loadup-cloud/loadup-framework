/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.support;

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

import java.util.Map;

import com.alipay.test.acts.component.CCInvoker;

/**
 * 测试统一上下文。
 * <p>
 * 其记录测试执行过程中的过程信息，方便各阶段处理。<br>
 * 其也会注入一些配置信息，比如校验规则等
 * 
 * @author dasong.jds
 * @version $Id: TestContext.java, v 0.1 2015年4月18日 下午5:07:47 dasong.jds Exp $
 */
public final class TestContext {

    /** 操作管理器，方便定制操作 */
    private final TestActionManager actionManager = new TestActionManager();

    /** 当前组件调用器 */
    private CCInvoker               ccInvoker;

    /** 属性信息 */
    private TestScope               tailScope;

    /**
     * 构造对象。
     * 
     */
    public TestContext() {
        super();
        tailScope = new TestScope(null);
    }

    /**
     * 根据name获取属性值。获取的时候，会自动转型
     * <p>
     * 框架使用
     * 
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public <E> E innerGetAttribute(String name) {
        return (E) tailScope.getAttribute(name);
    }

    /**
     * 设置属性。
     * 
     * @param name
     * @param value
     */
    public void setAttribute(String name, Object value) {
        tailScope.setAttribute(name, value);
    }

    /**
     * Getter method for property <tt>actionManager</tt>.
     * 
     * @return property value of actionManager
     */
    public TestActionManager getActionManager() {
        return actionManager;
    }

    /**
     * 获取当前所有可用的属性信息。
     * 
     * @return
     */
    public Map<String, Object> getAttributes() {

        return ccInvoker.getAvailableVariables();
    }

    /**
     * 获取当前可用属性。
     * <p>
     * 除了显示set的属性，还有ccil调用中传递的属性，ccil属性优先。
     * 
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public <E> E getAttribute(String name) {
        return (E) ccInvoker.getAvailableVariable(name);
    }

    /**
     * 压栈操作。
     * <p>
     * 创建新的隔离范围。
     * 请务必与{@link #pop()}联合使用。
     */
    public void push() {
        TestScope newTail = new TestScope(tailScope);
        tailScope = newTail;
    }

    /**
     * 出栈操作。
     * <p>
     * 回退至上一隔离范围。
     * 请务必与{@link #push()}联合使用。
     */
    public void pop() {
        TestScope parent = tailScope.getParent();
        tailScope = parent;
    }

    /**
     * 通过接口获取bean
     * 
     * @param interfaceClass 接口
     * @return bean
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> interfaceClass) {

        return (T) SofaRuntimeReference.getBean(interfaceClass);
    }

    /**
     * 通过名称获取bean
     * 
     * @param name      bean名称
     * @return bean
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        return (T) SofaRuntimeReference.getBean(name);
    }

    /**
     * 获取指定bundle的bean
     * 
     * @param name          bean名称
     * @param bundleName    bundle名称
     * @return bean
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, String bundleName) {
        return (T) SofaRuntimeReference.getBean(name, bundleName);
    }

    /**
     * 日志输出
     * 输出TestContext中的全部参数
     */
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("TestContext[");

        Map<String, Object> params = this.getAttributes();
        int i = 0;
        for (String key : params.keySet()) {
            if (i > 0) {
                str.append(",");
            }
            str.append(key);
            str.append("=");
            str.append(params.get(key));
            i++;
        }

        str.append("]");
        return str.toString();
    }

    /**
     * 获取所有属性，框架使用
     * 
     * @return
     */
    public Map<String, Object> innerGetAttributes() {
        return tailScope.getAttributes();
    }

    // getter & setter
    /**
     * Getter method for property <tt>ccInvoker</tt>.
     * 
     * @return property value of ccInvoker
     */
    public CCInvoker getCcInvoker() {
        return ccInvoker;
    }

    /**
     * Setter method for property <tt>ccInvoker</tt>.
     * 
     * @param ccInvoker value to be assigned to property ccInvoker
     */
    public void setCcInvoker(CCInvoker ccInvoker) {
        this.ccInvoker = ccInvoker;
    }

}
