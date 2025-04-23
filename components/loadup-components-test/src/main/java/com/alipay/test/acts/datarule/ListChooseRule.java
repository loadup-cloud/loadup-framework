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
 * 选择规则。
 * 
 * <p>
 * 格式：<br>
 * list(2,paytool) 代表从可用的支付工具中选择两条返回；<br>
 * list(2,tradeType,paytool) 代表从交易类型和支付工具的排列组合中随机选择两条；<br>
 * list(-1,paytool) 若count<=0代表全部穷举。
 * 
 * @author dasong.jds
 * @author zhiyuan.lzy
 * @version $Id: ListChooseRule.java, v 0.1 2015年10月24日 下午12:33:09 zhiyuan.lzy Exp $
 */
public class ListChooseRule implements RuleObject {

    /** 选取的条目数 */
    private int             rowCount;

    /** 作用的字段列表 */
    private List<FieldRule> fields = new ArrayList<FieldRule>();

    /**
     * 添加字段。
     * 
     * @param field
     * @param rule
     */
    public void addField(String field, RuleObject rule) {
        FieldRule f = new FieldRule();
        f.setFieldName(field);
        f.setRule(rule);

        this.fields.add(f);

    }

    /**
     * Getter method for property <tt>rowCount</tt>.
     * 
     * @return property value of rowCount
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * Setter method for property <tt>rowCount</tt>.
     * 
     * @param rowCount value to be assigned to property rowCount
     */
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * Getter method for property <tt>fields</tt>.
     * 
     * @return property value of fields
     */
    public List<FieldRule> getFields() {
        return fields;
    }

    /**
     * Setter method for property <tt>fields</tt>.
     * 
     * @param fields value to be assigned to property fields
     */
    public void setFields(List<FieldRule> fields) {
        this.fields = fields;
    }

}
