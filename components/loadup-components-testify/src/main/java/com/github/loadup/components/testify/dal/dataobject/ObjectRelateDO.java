/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.dal.dataobject;

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
 *
 *
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

    private Date gmtCreate;

    private Date gmtModify;

    /**
     *
     *
     * @return property value of id
     */
    public String getId() {
        return id;
    }

    /**
     *
     *
     * @param id value to be assigned to property id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     *
     * @return property value of system
     */
    public String getSystem() {
        return system;
    }

    /**
     *
     *
     * @param system value to be assigned to property system
     */
    public void setSystem(String system) {
        this.system = system;
    }

    /**
     *
     *
     * @return property value of modelType
     */
    public String getModelType() {
        return modelType;
    }

    /**
     *
     *
     * @param modelType value to be assigned to property modelType
     */
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    /**
     *
     *
     * @return property value of modelObj
     */
    public String getModelObj() {
        return modelObj;
    }

    /**
     *
     *
     * @param modelObj value to be assigned to property modelObj
     */
    public void setModelObj(String modelObj) {
        this.modelObj = modelObj;
    }

    /**
     *
     *
     * @return property value of modelData
     */
    public String getModelData() {
        return modelData;
    }

    /**
     *
     *
     * @param modelData value to be assigned to property modelData
     */
    public void setModelData(String modelData) {
        this.modelData = modelData;
    }

    /**
     *
     *
     * @return property value of dataDesc
     */
    public String getDataDesc() {
        return dataDesc;
    }

    /**
     *
     *
     * @param dataDesc value to be assigned to property dataDesc
     */
    public void setDataDesc(String dataDesc) {
        this.dataDesc = dataDesc;
    }

    /**
     *
     *
     * @return property value of objFlag
     */
    public String getObjFlag() {
        return objFlag;
    }

    /**
     *
     *
     * @param objFlag value to be assigned to property objFlag
     */
    public void setObjFlag(String objFlag) {
        this.objFlag = objFlag;
    }

    /**
     *
     *
     * @return property value of relateSourceId
     */
    public String getRelateSourceId() {
        return relateSourceId;
    }

    /**
     *
     *
     * @param relateSourceId value to be assigned to property relateSourceId
     */
    public void setRelateSourceId(String relateSourceId) {
        this.relateSourceId = relateSourceId;
    }

    /**
     *
     *
     * @return property value of sourceData
     */
    public String getSourceData() {
        return sourceData;
    }

    /**
     *
     *
     * @param sourceData value to be assigned to property sourceData
     */
    public void setSourceData(String sourceData) {
        this.sourceData = sourceData;
    }

    /**
     *
     *
     * @return property value of memo
     */
    public String getMemo() {
        return memo;
    }

    /**
     *
     *
     * @param memo value to be assigned to property memo
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }

    /**
     *
     *
     * @return property value of gmtCreate
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     *
     *
     * @param gmtCreate value to be assigned to property gmtCreate
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     *
     *
     * @return property value of gmtModify
     */
    public Date getGmtModify() {
        return gmtModify;
    }

    /**
     *
     *
     * @param gmtModify value to be assigned to property gmtModify
     */
    public void setGmtModify(Date gmtModify) {
        this.gmtModify = gmtModify;
    }
}
