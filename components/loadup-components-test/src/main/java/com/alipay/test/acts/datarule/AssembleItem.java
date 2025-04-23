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

import java.util.ArrayList;
import java.util.List;

/**
 * 组装项。
 * 
 * @author dasong.jds
 * @version $Id: AssembleItem.java, v 0.1 2015年10月25日 上午11:04:27 dasong.jds Exp $
 */
public class AssembleItem {

    /** 组装条件 */
    private AssembleCondition   condition;

    /** 组装值 */
    private List<AssembleValue> values = new ArrayList<AssembleValue>();

    /**
     * 添加值。
     * 
     * @param value
     */
    public void addValue(AssembleValue value) {
        this.values.add(value);
    }

    /**
     * Getter method for property <tt>condition</tt>.
     * 
     * @return property value of condition
     */
    public AssembleCondition getCondition() {
        return condition;
    }

    /**
     * Setter method for property <tt>condition</tt>.
     * 
     * @param condition value to be assigned to property condition
     */
    public void setCondition(AssembleCondition condition) {
        this.condition = condition;
    }

    /**
     * Getter method for property <tt>values</tt>.
     * 
     * @return property value of values
     */
    public List<AssembleValue> getValues() {
        return values;
    }

    /**
     * Setter method for property <tt>values</tt>.
     * 
     * @param values value to be assigned to property values
     */
    public void setValues(List<AssembleValue> values) {
        this.values = values;
    }

}
