/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.dal.dataobject;

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
 * 原子数据对象DO
 * 
 * @author hongling.xiang
 * @version $Id: OrgDbDO.java, v 0.1 2015年9月25日 上午12:07:47 hongling.xiang Exp $
 */
public class OrgDbDO {

    /** 
     * This property corresponds to db column source_id 
     */
    private String sourceId;

    /** 
     * This property corresponds to db column system 
     */
    private String system;

    /** 
     * This property corresponds to db column source_data 
     */
    private String sourceData;

    /** 
     * This property corresponds to db column source_rule 
     */
    private String sourceRule;

    /** 
     * This property corresponds to db column gmt_create 
     */
    private Date   gmtCreate;

    /** 
     * This property corresponds to db column gmt_modify
     */
    private Date   gmtModify;

    /** 
     * This property corresponds to db column memo 
     */
    private String memo;

    /**
     * Getter method for property <tt>sourceId</tt>.
     * 
     * @return property value of sourceId
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Setter method for property <tt>sourceId</tt>.
     * 
     * @param sourceId value to be assigned to property sourceId
     */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
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
     * Getter method for property <tt>sourceData</tt>.
     * 
     * @return property value of sourceData
     */
    public String getSourceData() {
        return sourceData;
    }

    /**
     * Setter method for property <tt>sourceData</tt>.
     * 
     * @param sourceData value to be assigned to property sourceData
     */
    public void setSourceData(String sourceData) {
        this.sourceData = sourceData;
    }

    /**
     * Getter method for property <tt>sourceRule</tt>.
     * 
     * @return property value of sourceRule
     */
    public String getSourceRule() {
        return sourceRule;
    }

    /**
     * Setter method for property <tt>sourceRule</tt>.
     * 
     * @param sourceRule value to be assigned to property sourceRule
     */
    public void setSourceRule(String sourceRule) {
        this.sourceRule = sourceRule;
    }

    /**
     * Getter method for property <tt>gmtCreate</tt>.
     * 
     * @return property value of gmtCreate
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * Setter method for property <tt>gmtCreate</tt>.
     * 
     * @param gmtCreate value to be assigned to property gmtCreate
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * Getter method for property <tt>gmtModify</tt>.
     * 
     * @return property value of gmtModify
     */
    public Date getGmtModify() {
        return gmtModify;
    }

    /**
     * Setter method for property <tt>gmtModify</tt>.
     * 
     * @param gmtModify value to be assigned to property gmtModify
     */
    public void setGmtModify(Date gmtModify) {
        this.gmtModify = gmtModify;
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

}
