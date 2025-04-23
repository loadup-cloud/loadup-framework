package com.alipay.test.acts.object.comparer.impl;

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

import com.alipay.test.acts.object.comparer.UnitComparer;

/**
 *
 * "C" flag 校验器
 *
 * @author midang
 *
 * @version $Id: ConditionComparer.java,v 0.1 2010-8-12 上午11:52:43 midang Exp $
 */
public class ConditionComparer implements UnitComparer {

    /**
     * 不做比较
     * 
     * <pre>
     * compare("null",null)     = true
     * compare("null","null")   = true
     * compare("abc","abc")     = true
     * </pre>
     * 
     * @param expect
     * @param actual
     * @return
     */
    @Override
    public boolean compare(Object expect, Object actual, String comparerFlagCode) {
        return true;
    }

}
