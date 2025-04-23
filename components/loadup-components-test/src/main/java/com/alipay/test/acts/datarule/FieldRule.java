/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.datarule;

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
 * 字段赋值规则。
 * <p>
 * A={a1,a2} <br>
 * 规则目前支持range、array
 * 
 * @author dasong.jds
 * @version $Id: FieldRule.java, v 0.1 2015年11月1日 下午6:05:06 dasong.jds Exp $
 */
public class FieldRule implements RuleObject {

    /** 字段 */
    private String     fieldName;

    /** 赋值规则 */
    private RuleObject rule = null;

    /**
     * Getter method for property <tt>fieldName</tt>.
     * 
     * @return property value of fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Setter method for property <tt>fieldName</tt>.
     * 
     * @param fieldName value to be assigned to property fieldName
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Getter method for property <tt>rule</tt>.
     * 
     * @return property value of rule
     */
    public RuleObject getRule() {
        return rule;
    }

    /**
     * Setter method for property <tt>rule</tt>.
     * 
     * @param rule value to be assigned to property rule
     */
    public void setRule(RuleObject rule) {
        this.rule = rule;
    }

}
