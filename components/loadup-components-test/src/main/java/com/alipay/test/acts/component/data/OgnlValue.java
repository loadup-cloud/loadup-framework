/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.data;

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

import ognl.Ognl;
import ognl.OgnlException;

import com.alipay.test.acts.manage.TestLogger;

/**
 * ognl形式的值对象。
 * 
 * @author dasong.jds
 * @version $Id: OgnlValue.java, v 0.1 2015年6月9日 下午11:01:05 dasong.jds Exp $
 */
public class OgnlValue {

    /** ognl */
    private final String ognl;

    /**
     * @param ognl
     */
    public OgnlValue(String ognl) {
        super();
        this.ognl = ognl;
    }

    public Object toObject(Object target) {
        try {
            return Ognl.getValue(ognl, target);
        } catch (OgnlException e) {
            TestLogger.getLogger().warn(e, "ognl get value error[ognl=", ognl, "]");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T toObject(Object target, Class<T> clazz) {
        try {
            return (T) Ognl.getValue(ognl, target, clazz);
        } catch (OgnlException e) {
            TestLogger.getLogger().warn(e, "ognl get value error[ognl=", ognl, "]");
            return null;
        }
    }
}
