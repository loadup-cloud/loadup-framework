/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.dal.table;

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
 * 用于从csv中填充规则的时候使用
 * 
 * @author zhiyuan.lzy
 * @version $Id: CsvFillObject.java, v 0.1 2015年10月27日 下午11:18:11 zhiyuan.lzy Exp $
 */
public class CsvFillObject {

    private String system;

    private String model_obj;

    private String model_data;

    private String data_rule;

    /**
     * Getter method for property <tt>system</tt>.
     * 
     * @return property value of system
     */
    public String getSystem() {
        return system;
    }

    /**
     * Setter method for property <tt>system</tt>.
     * 
     * @param system value to be assigned to property system
     */
    public void setSystem(String system) {
        this.system = system;
    }

    /**
     * Getter method for property <tt>model_obj</tt>.
     * 
     * @return property value of model_obj
     */
    public String getModel_obj() {
        return model_obj;
    }

    /**
     * Setter method for property <tt>model_obj</tt>.
     * 
     * @param model_obj value to be assigned to property model_obj
     */
    public void setModel_obj(String model_obj) {
        this.model_obj = model_obj;
    }

    /**
     * Getter method for property <tt>model_data</tt>.
     * 
     * @return property value of model_data
     */
    public String getModel_data() {
        return model_data;
    }

    /**
     * Setter method for property <tt>model_data</tt>.
     * 
     * @param model_data value to be assigned to property model_data
     */
    public void setModel_data(String model_data) {
        this.model_data = model_data;
    }

    public String getData_rule() {
        return data_rule;
    }

    public void setData_rule(String data_rule) {
        this.data_rule = data_rule;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "CsvFillObject [system=" + system + ", model_obj=" + model_obj + ", model_data="
               + model_data + ", data_rule=" + data_rule + "]";
    }

}
