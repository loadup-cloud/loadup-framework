/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.model;

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
 * 自定义参数对象
 *
 * @author tantian.wc
 *
 */
public class VirtualParams extends TestUnit {

    /**
     * 自定义参数
     */
    public Map<String, VirtualObject> params = new HashMap<String, VirtualObject>();

    public VirtualParams() {
        super();
    }

    public VirtualParams(Map<String, Object> params) {
        if (null == params) {
            return;
        }
        for (String key : params.keySet()) {
            addParam(key, params.get(key));
        }
    }

    public static VirtualParams getInstance() {
        return new VirtualParams();
    }

    public Map<String, VirtualObject> getParams() {
        return params;
    }

    public void setParams(Map<String, VirtualObject> params) {
        this.params = params;
    }

    public void addParam(String key, Object obj) {
        params.put(key, new VirtualObject(obj));
    }

    public Object getByParaName(final String paraName) {
        if (params == null || null == paraName) {
            return null;
        }
        return params.get(paraName);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "VirtualParams [params=" + params + "]";
    }
}
