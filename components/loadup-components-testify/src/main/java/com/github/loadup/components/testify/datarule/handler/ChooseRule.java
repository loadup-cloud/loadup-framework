/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.datarule.handler;

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

import com.github.loadup.components.testify.datarule.RuleObject;
import java.util.ArrayList;
import java.util.List;

/**
 * 选择规则。
 *
 * <p>
 * 格式：<br>
 * choose(2,paytool) 代表从可用的支付工具中选择两条返回；<br>
 * choose(2,tradeType,paytool) 代表从交易类型和支付工具的排列组合中随机选择两条；<br>
 * choose(-1,paytool) 若count<=0代表全部穷举。
 *
 *
 *
 */
public class ChooseRule implements RuleObject {

    /**
     * 选取的条目数
     */
    private int itemCount;

    /**
     * 作用的字段列表
     */
    private List<String> fields = new ArrayList<String>();

    private List<String> defaultValues = new ArrayList<String>();

    /**
     * 添加字段默认值,解析的时候填充。
     *
     * @param defaultValue
     */
    public void addDefaultValue(String defaultValue) {

        this.defaultValues.add(defaultValue);
    }

    /**
     *
     *
     * @return property value of defaultValues
     */
    public List<String> getDefaultValues() {
        return defaultValues;
    }

    /**
     *
     *
     * @param defaultValues value to be assigned to property defaultValues
     */
    public void setDefaultValues(List<String> defaultValues) {
        this.defaultValues = defaultValues;
    }

    /**
     * 添加字段。
     *
     * @param field
     */
    public void addField(String field) {

        this.fields.add(field);
    }

    /**
     *
     *
     * @return property value of itemCount
     */
    public int getItemCount() {
        return itemCount;
    }

    /**
     *
     *
     * @param itemCount value to be assigned to property itemCount
     */
    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    /**
     *
     *
     * @return property value of fields
     */
    public List<String> getFields() {
        return fields;
    }

    /**
     *
     *
     * @param fields value to be assigned to property fields
     */
    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}
