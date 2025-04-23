/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.ccil;

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

/**
 * bean调用。
 * 
 * @author dasong.jds
 * @version $Id: CcilBeanCall.java, v 0.1 2015年7月21日 下午5:42:40 dasong.jds Exp $
 */
public class CcilBeanCall implements CcilExpr {

    /** bean 名称*/
    private String                beanName;

    /** 方法名称 */
    private String                methodName;

    /** bundle */
    private String                bundle;

    /** 调用参数 */
    private List<CcilInvokeParam> params;

    /**
     * Getter method for property <tt>beanName</tt>.
     * 
     * @return property value of beanName
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     * Setter method for property <tt>beanName</tt>.
     * 
     * @param beanName value to be assigned to property beanName
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * Getter method for property <tt>methodName</tt>.
     * 
     * @return property value of methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Setter method for property <tt>methodName</tt>.
     * 
     * @param methodName value to be assigned to property methodName
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Getter method for property <tt>bundle</tt>.
     * 
     * @return property value of bundle
     */
    public String getBundle() {
        return bundle;
    }

    /**
     * Setter method for property <tt>bundle</tt>.
     * 
     * @param bundle value to be assigned to property bundle
     */
    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    /**
     * Getter method for property <tt>params</tt>.
     * 
     * @return property value of params
     */
    public List<CcilInvokeParam> getParams() {
        return params;
    }

    /**
     * Setter method for property <tt>params</tt>.
     * 
     * @param params value to be assigned to property params
     */
    public void setParams(List<CcilInvokeParam> params) {
        this.params = params;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CcilBeanCall[beanName=");
        builder.append(beanName);
        builder.append(",methodName=");
        builder.append(methodName);
        builder.append(",bundle=");
        builder.append(bundle);
        builder.append(",params=");
        builder.append(params);
        builder.append("]");
        return builder.toString();
    }

}
