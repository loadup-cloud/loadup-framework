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
 * 数据关联关系DO
 * 
 * @author hongling.xiang
 * @version $Id: ObjectRelateDO.java, v 0.1 2015年9月26日 上午8:34:01 hongling.xiang Exp $
 */
public class ObjectRelateDO {

    private String id;

    private String system;

    private String modelObj;

    private String modelData;

    private String modelType;

    private String dataDesc;

    private String objFlag;

    private String relateSourceId;

    private String sourceData;

    private String memo;

    private Date   gmtCreate;

    private Date   gmtModify;

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
     * Getter method for property <tt>modelType</tt>.
     * 
     * @return property value of modelType
     */
    public String getModelType() {
        return modelType;
    }

    /**
     * Setter method for property <tt>modelType</tt>.
     * 
     * @param modelType value to be assigned to property modelType
     */
    public void setModelType(String modelType) {
        this.modelType = modelType;
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
     * Getter method for property <tt>modelObj</tt>.
     * 
     * @return property value of modelObj
     */
    public String getModelObj() {
        return modelObj;
    }

    /**
     * Setter method for property <tt>modelObj</tt>.
     * 
     * @param modelObj value to be assigned to property modelObj
     */
    public void setModelObj(String modelObj) {
        this.modelObj = modelObj;
    }

    /**
     * Getter method for property <tt>modelData</tt>.
     * 
     * @return property value of modelData
     */
    public String getModelData() {
        return modelData;
    }

    /**
     * Setter method for property <tt>modelData</tt>.
     * 
     * @param modelData value to be assigned to property modelData
     */
    public void setModelData(String modelData) {
        this.modelData = modelData;
    }

    /**
     * Getter method for property <tt>dataDesc</tt>.
     * 
     * @return property value of dataDesc
     */
    public String getDataDesc() {
        return dataDesc;
    }

    /**
     * Setter method for property <tt>dataDesc</tt>.
     * 
     * @param dataDesc value to be assigned to property dataDesc
     */
    public void setDataDesc(String dataDesc) {
        this.dataDesc = dataDesc;
    }

    /**
     * Getter method for property <tt>objFlag</tt>.
     * 
     * @return property value of objFlag
     */
    public String getObjFlag() {
        return objFlag;
    }

    /**
     * Setter method for property <tt>objFlag</tt>.
     * 
     * @param objFlag value to be assigned to property objFlag
     */
    public void setObjFlag(String objFlag) {
        this.objFlag = objFlag;
    }

    /**
     * Getter method for property <tt>relateSourceId</tt>.
     * 
     * @return property value of relateSourceId
     */
    public String getRelateSourceId() {
        return relateSourceId;
    }

    /**
     * Setter method for property <tt>relateSourceId</tt>.
     * 
     * @param relateSourceId value to be assigned to property relateSourceId
     */
    public void setRelateSourceId(String relateSourceId) {
        this.relateSourceId = relateSourceId;
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

}
