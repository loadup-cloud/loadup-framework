/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component;

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

import java.util.HashMap;
import java.util.Map;

import com.alipay.test.acts.component.data.OgnlValue;
import com.alipay.test.acts.component.data.TextValue;
import com.alipay.test.acts.support.TestContext;

/**
 * 调用上下文。
 * <p>
 * 贯穿整个测试过程，管理scope的调用栈。
 * 
 * @author dasong.jds
 * @version $Id: InvokeContext.java, v 0.1 2015年5月27日 上午10:07:52 dasong.jds Exp $
 */
public class InvokeContext {

    /** 测试上下文 */
    private final TestContext testContext;

    /** 关联的invoker */
    private final CCInvoker   refInvoker;

    /** 当前scope，初始为空。使用之前必须先压栈。 */
    private InvokeScope       tailScope = null;

    /**
     * 构造对象。
     * 
     * @param testContext
     * @param refInvoker
     */
    InvokeContext(TestContext testContext, CCInvoker refInvoker) {
        super();
        this.testContext = testContext;
        this.refInvoker = refInvoker;
    }

    /**
     * 设置变量。
     * 
     * @param name          变量名
     * @param value         变量值
     */
    public void setVariable(String name, Object value) {
        this.tailScope.setVariable(name, value);
    }

    /**
     * 设置变量到父对象。
     * <p>
     * 若父对象为空，设置到测试上下文。
     * 
     * @param name          变量名
     * @param value         变量值
     */
    public void setVariable2Parent(String name, Object value) {
        if (this.tailScope.getParent() != null) {
            this.tailScope.getParent().setVariable(name, value);
        } else {
            this.testContext.setAttribute(name, value);
        }
    }

    /**
     * 获取变量值。
     * <p>
     * 优先从scope获取，若不存在则寻找testConext。
     * 
     * @param name
     * @return
     */
    public Object getVariable(String name) {
        if (tailScope != null && this.tailScope.containsVariable(name)) {
            return this.tailScope.getVariable(name);
        } else {
            return this.testContext.innerGetAttribute(name);
        }
    }

    /**
     * 获取真实变量值。
     * <p>
     * 对于特定类型进行解析。
     * 
     * @param name
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getAvailableVariable(String name, Class<T> clazz) {
        Object v = getVariable(name);
        T t;
        if (v instanceof TextValue) {
            t = ((TextValue) v).toObject(clazz, getVariables());
        } else if (v instanceof OgnlValue) {
            t = (T) ((OgnlValue) v).toObject(getVariables());
        } else {
            t = (T) v;
        }
        // 处理完放入当前栈，当前调用内有效。避免多次重复解析。
        if (t != v) {
            setVariable(name, t);
        }
        return t;

    }

    /**
     * 获取真实变量值。
     * <p>
     * 对于特定类型进行解析。
     * 
     * @param name
     * @return
     */
    public Object getAvailableVariable(String name) {
        Object v = getVariable(name);
        Object t;
        if (v instanceof TextValue) {
            t = ((TextValue) v).toObject(getVariables());
        } else if (v instanceof OgnlValue) {
            t = ((OgnlValue) v).toObject(getVariables());
        } else {
            t = v;
        }
        // 处理完放入当前栈，当前调用内有效。避免多次重复解析。
        if (t != v) {
            setVariable(name, t);
        }
        return t;

    }

    /**
     * 获取当前所有变量值，不做类型解析。
     * 
     * @return
     */
    private Map<String, Object> getVariables() {

        Map<String, Object> vars = new HashMap<String, Object>();

        vars.putAll(testContext.innerGetAttributes());

        if (tailScope != null) {
            vars.putAll(tailScope.getVariables());
        }

        return vars;
    }

    /**
     * 获取所有可用变量值。
     * <p>
     * 返回前做类型解析。
     * 
     * @return
     */
    public Map<String, Object> getAvailableVariables() {

        Map<String, Object> availableVars = getVariables();

        // 返回前暴力处理，后期优化
        for (Map.Entry<String, Object> entry : availableVars.entrySet()) {
            Object v = getAvailableVariable(entry.getKey());
            if (v != entry.getValue()) {
                entry.setValue(v);
            }
        }
        return availableVars;
    }

    /**
     * 栈操作：压栈
     * 
     * @return  新产生的scope
     */
    public void push() {
        InvokeScope newTail = new InvokeScope(tailScope);
        tailScope = newTail;
    }

    /**
     * 栈操作：出栈。
     * 
     * @return  上一scope
     */
    public void pop() {
        InvokeScope parent = tailScope.getParent();
        tailScope = parent;
    }

    /**
     * clone当前操作镜像。
     * 
     * @return
     */
    public InvokeContext cloneWithVariables() {
        InvokeContext invokeContext = new InvokeContext(this.testContext, refInvoker);
        invokeContext.tailScope = new InvokeScope(null);
        invokeContext.tailScope.setVariables(getVariables());

        return invokeContext;

    }

    // ~~~ bean方法
    /**
     * Getter method for property <tt>testContext</tt>.
     * 
     * @return property value of testContext
     */
    public TestContext getTestContext() {
        return testContext;
    }

    /**
     * Getter method for property <tt>refInvoker</tt>.
     * 
     * @return property value of refInvoker
     */
    public CCInvoker getRefInvoker() {
        return refInvoker;
    }

}
