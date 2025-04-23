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

import java.util.Date;

/**
 * 
 * @author hongling.xiang
 * @version $Id: OrgDb.java, v 0.1 2015年10月10日 下午11:51:11 hongling.xiang Exp $
 */
public class OrgDb {

    /** 
     * This property corresponds to db column source_id 
     */
    private String source_id;

    /** 
     * This property corresponds to db column system 
     */
    private String system;

    /** 
     * This property corresponds to db column source_data 
     */
    private String source_data;

    /** 
     * This property corresponds to db column source_rule 
     */
    private String source_rule;

    /** 
     * This property corresponds to db column gmt_create 
     */
    private Date   gmt_create;

    /** 
     * This property corresponds to db column gmt_modify
     */
    private Date   gmt_modify;

    /** 
     * This property corresponds to db column memo 
     */
    private String memo;

    /**
     * Getter method for property <tt>source_id</tt>.
     * 
     * @return property value of source_id
     */
    public String getSource_id() {
        return source_id;
    }

    /**
     * Setter method for property <tt>source_id</tt>.
     * 
     * @param source_id value to be assigned to property source_id
     */
    public void setSource_id(String source_id) {
        this.source_id = source_id;
    }

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
     * Getter method for property <tt>source_data</tt>.
     * 
     * @return property value of source_data
     */
    public String getSource_data() {
        return source_data;
    }

    /**
     * Setter method for property <tt>source_data</tt>.
     * 
     * @param source_data value to be assigned to property source_data
     */
    public void setSource_data(String source_data) {
        this.source_data = source_data;
    }

    /**
     * Getter method for property <tt>source_rule</tt>.
     * 
     * @return property value of source_rule
     */
    public String getSource_rule() {
        return source_rule;
    }

    /**
     * Setter method for property <tt>source_rule</tt>.
     * 
     * @param source_rule value to be assigned to property source_rule
     */
    public void setSource_rule(String source_rule) {
        this.source_rule = source_rule;
    }

    /**
     * Getter method for property <tt>gmt_create</tt>.
     * 
     * @return property value of gmt_create
     */
    public Date getGmt_create() {
        return gmt_create;
    }

    /**
     * Setter method for property <tt>gmt_create</tt>.
     * 
     * @param gmt_create value to be assigned to property gmt_create
     */
    public void setGmt_create(Date gmt_create) {
        this.gmt_create = gmt_create;
    }

    /**
     * Getter method for property <tt>gmt_modify</tt>.
     * 
     * @return property value of gmt_modify
     */
    public Date getGmt_modify() {
        return gmt_modify;
    }

    /**
     * Setter method for property <tt>gmt_modify</tt>.
     * 
     * @param gmt_modify value to be assigned to property gmt_modify
     */
    public void setGmt_modify(Date gmt_modify) {
        this.gmt_modify = gmt_modify;
    }

    /**
     * Getter method for property <tt>memo</tt>.
     * 
     * @return property value of memo
     */
    public String getMemo() {
        return memo;
    }

    /**
     * Setter method for property <tt>memo</tt>.
     * 
     * @param memo value to be assigned to property memo
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "OrgDb [source_id=" + source_id + ", system=" + system + ", source_data="
               + source_data + ", source_rule=" + source_rule + ", gmt_create=" + gmt_create
               + ", gmt_modify=" + gmt_modify + ", memo=" + memo + "]";
    }

}
