/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.message.script.parser.groovy;

/*-
 * #%L
 * loadup-components-gateway-core
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
import java.util.Iterator;
import java.util.Map;

/**
 * 需要动态加载到Spring中的Bean配置信息
 */
public final class DynamicBean {

    /**
     * 存储属性的map
     */
    private final Map<String, String> properties = new HashMap<String, String>();

    /**
     * 添加属性
     */
    public void put(String key, String value) {
        properties.put(key, value);
    }

    /**
     * 遍历属性
     */
    public Iterator<String> keyIterator() {
        return properties.keySet().iterator();
    }

    /**
     * 返回属性值
     */
    public String get(String key) {
        return properties.get(key);
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        StringBuffer retValue = new StringBuffer("DynamicBean[properties=");
        retValue.append(this.properties);
        retValue.append(']');
        return retValue.toString();
    }
}
