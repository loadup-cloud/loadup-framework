/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.support;

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
 * 测试范围。
 * <p>
 * 用于支撑区域化需求，设置只范围内有效。
 *
 * 
 *
 */
public class TestScope {

    /**
     * 属性存储
     */
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * 父对象
     */
    private final TestScope parent;

    /**
     * 构造子范围对象。
     *
     * @param parent
     * @param refInvoker
     */
    public TestScope(TestScope parent) {
        super();
        this.parent = parent;
    }

    /**
     * 设置属性。
     *
     * @param name  变量名
     * @param value 变量值
     */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    /**
     * 获取属性值。
     * <p>
     * 优先从本对象获取，若不存在则寻找父对象。
     *
     * @param name
     * @return
     */
    public Object getAttribute(String name) {
        if (this.attributes.containsKey(name)) {
            return this.attributes.get(name);
        }

        if (parent != null) {
            return parent.getAttribute(name);
        }

        return null;
    }

    /**
     * 判断属性是否存在。
     *
     * @param name
     * @return
     */
    public boolean containsAttribute(String name) {
        if (this.attributes.containsKey(name)) {
            return true;
        }

        if (parent != null) {
            return parent.containsAttribute(name);
        }

        return false;
    }

    /**
     * 获取范围所有变量值。
     *
     * @return
     */
    public Map<String, Object> getAttributes() {

        // 存在优先级，先父节点再子节点
        Map<String, Object> scopeAttributes = new HashMap<String, Object>();
        if (parent != null) {
            scopeAttributes.putAll(parent.getAttributes());
        }

        scopeAttributes.putAll(attributes);

        return scopeAttributes;
    }

    /**
     * 
     *
     * @return property value of parent
     */
    public TestScope getParent() {
        return parent;
    }

}
