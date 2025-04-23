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

import com.alipay.test.acts.exception.CcilParseException;

/**
 * 引用类型表达式。
 * 
 * @author dasong.jds
 * @version $Id: CcilVariantRef.java, v 0.1 2015年5月24日 下午10:20:01 dasong.jds Exp $
 */
public class CcilVariantRef implements CcilExpr {

    /** ognl*/
    private final String ognl;

    /**
     * 构造关联表达式。
     * 
     * @param expr
     */
    public CcilVariantRef(String expr) {

        // ${xxx.xxx} 去除修饰
        if (expr.startsWith("${")) {
            ognl = expr = expr.substring(2, expr.length() - 1);
        } else if (expr.startsWith("#")) {
            ognl = expr = expr.substring(1, expr.length());
        } else {
            throw new CcilParseException("not support variant[expr=" + expr + "]");
        }
    }

    /**
     * Getter method for property <tt>ognl</tt>.
     * 
     * @return property value of ognl
     */
    public String getOgnl() {
        return ognl;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CcilVariantRefExpr[ognl=");
        builder.append(ognl);
        builder.append("]");
        return builder.toString();
    }

}
