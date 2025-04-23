/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.impl.clear;

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

import com.alipay.test.acts.component.CCComponent;
import com.alipay.test.acts.component.annotation.CCClear;
import com.alipay.test.acts.component.impl.CCComponentImpl;
import com.alipay.test.acts.component.impl.RepositoryCallback;

/**
 * 清理组件处理。
 * 
 * @author dasong.jds
 * @version $Id: ClearRepositoryCallback.java, v 0.1 2015年5月30日 下午10:05:56 dasong.jds Exp $
 */
public class ClearRepositoryCallback implements RepositoryCallback<CCClear> {

    /** 
     * @see com.ipay.itest.common.repository.impl.RepositoryCallback#getCCType()
     */
    @Override
    public Class<CCClear> getCCType() {
        return CCClear.class;
    }

    /** 
     * @see com.ipay.itest.common.repository.impl.RepositoryCallback#createComponent(java.lang.annotation.Annotation, Object, Method)
     */
    @Override
    public CCComponent createComponent(CCClear ccDesc, Object ccHost, Method ccMethod) {
        return new CCComponentImpl(ccDesc.id(), ccHost, ccMethod);
    }

}
