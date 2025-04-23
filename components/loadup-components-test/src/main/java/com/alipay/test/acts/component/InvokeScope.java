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

/**
 * 调用范围。
 * <p>
 * 存储调用过程中变量值。主要组件间调用隔离。
 * 
 * @author dasong.jds
 * @version $Id: InvokeScope.java, v 0.1 2015年5月23日 下午9:59:33 dasong.jds Exp $
 */
class InvokeScope {

    /** 变量存储 */
    private final Map<String, Object> variables = new HashMap<String, Object>();

    /** 父对象 */
    private final InvokeScope         parent;

    /**
     * 构造子范围对象。
     * 
     * @param parent
     * @param refInvoker
     */
    public InvokeScope(InvokeScope parent) {
        super();
        this.parent = parent;
    }

    /**
     * 设置变量。
     * 
     * @param name          变量名
     * @param value         变量值
     */
    public void setVariable(String name, Object value) {
        this.variables.put(name, value);
    }

    /**
     * 设置变量集合。
     * 
     * @param vars
     */
    public void setVariables(Map<String, Object> vars) {
        this.variables.putAll(vars);
    }

    /**
     * 获取变量值。
     * <p>
     * 优先从本对象获取，若不存在则寻找父对象。
     * 
     * @param name
     * @return
     */
    public Object getVariable(String name) {
        if (this.variables.containsKey(name)) {
            return this.variables.get(name);
        }

        if (parent != null) {
            return parent.getVariable(name);
        }

        return null;
    }

    /**
     * 判断变量是否存在。
     * 
     * @param name
     * @return
     */
    public boolean containsVariable(String name) {
        if (this.variables.containsKey(name)) {
            return true;
        }

        if (parent != null) {
            return parent.containsVariable(name);
        }

        return false;
    }

    /**
     * 获取范围所有变量值。
     * 
     * @return
     */
    public Map<String, Object> getVariables() {

        // 存在优先级，先父节点再子节点
        Map<String, Object> scopeVariables = new HashMap<String, Object>();
        if (parent != null) {
            scopeVariables.putAll(parent.getVariables());
        }

        scopeVariables.putAll(variables);

        return scopeVariables;
    }

    /**
     * Getter method for property <tt>parent</tt>.
     * 
     * @return property value of parent
     */
    public InvokeScope getParent() {
        return parent;
    }

}
