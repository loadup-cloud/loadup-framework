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

/**
 * 赋值表达式。
 * 
 * @author dasong.jds
 * @version $Id: CcilAssignExpr.java, v 0.1 2015年5月26日 下午9:52:54 dasong.jds Exp $
 */
public class CcilAssign implements CcilExpr {

    /** 变量名 */
    private String       name;

    /** invoke表达式 */
    private CcilInvoke   ccilInvoke;

    /** bean call */
    private CcilBeanCall beanCall;

    /**
     * Getter method for property <tt>name</tt>.
     * 
     * @return property value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name</tt>.
     * 
     * @param name value to be assigned to property name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for property <tt>ccilInvoke</tt>.
     * 
     * @return property value of ccilInvoke
     */
    public CcilInvoke getCcilInvoke() {
        return ccilInvoke;
    }

    /**
     * Setter method for property <tt>ccilInvoke</tt>.
     * 
     * @param ccilInvoke value to be assigned to property ccilInvoke
     */
    public void setCcilInvoke(CcilInvoke ccilInvoke) {
        this.ccilInvoke = ccilInvoke;
    }

    /**
     * Getter method for property <tt>beanCall</tt>.
     * 
     * @return property value of beanCall
     */
    public CcilBeanCall getBeanCall() {
        return beanCall;
    }

    /**
     * Setter method for property <tt>beanCall</tt>.
     * 
     * @param beanCall value to be assigned to property beanCall
     */
    public void setBeanCall(CcilBeanCall beanCall) {
        this.beanCall = beanCall;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CcilAssignExpr[name=");
        builder.append(name);

        if (ccilInvoke != null) {
            builder.append(",ccilInvoke=");
            builder.append(ccilInvoke);
        }
        if (beanCall != null) {
            builder.append(",beanCall=");
            builder.append(beanCall);
        }

        builder.append("]");
        return builder.toString();
    }

}
