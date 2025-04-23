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
 * 用例生成规则表
 * 
 * @author hongling.xiang
 * @version $Id: CaseRule.java, v 0.1 2015年10月15日 上午22:38:17 hongling.xiang Exp $
 */
public class CaseRule {

    /** 主键 */
    private String id;

    /** 所属系统 */
    private String system;

    /** 对象名称 */
    private String model_obj;

    /** 用例规则 */
    private String case_rule;

    /** 状态标识 */
    private String status;

    /** 优先级 */
    private String priority;

    /** 备注 */
    private String memo;

    /** 创建时间 */
    private Date   gmt_create;

    /** 修改时间 */
    private Date   gmt_modify;

    /**
     * Getter method for property <tt>id</tt>.
     * 
     * @return property value of id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter method for property <tt>id</tt>.
     * 
     * @param id value to be assigned to property id
     */
    public void setId(String id) {
        this.id = id;
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
     * Getter method for property <tt>case_rule</tt>.
     * 
     * @return property value of case_rule
     */
    public String getCase_rule() {
        return case_rule;
    }

    /**
     * Setter method for property <tt>case_rule</tt>.
     * 
     * @param case_rule value to be assigned to property case_rule
     */
    public void setCase_rule(String case_rule) {
        this.case_rule = case_rule;
    }

    /**
     * Getter method for property <tt>priority</tt>.
     * 
     * @return property value of priority
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Setter method for property <tt>priority</tt>.
     * 
     * @param priority value to be assigned to property priority
     */
    public void setPriority(String priority) {
        this.priority = priority;
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
     * Getter method for property <tt>status</tt>.
     * 
     * @return property value of status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter method for property <tt>status</tt>.
     * 
     * @param status value to be assigned to property status
     */
    public void setStatus(String status) {
        this.status = status;
    }

}
