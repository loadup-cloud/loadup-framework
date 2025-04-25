/**
 
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
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
 * 用例规则DO
 *
 * 
 *
 */
public class CaseRuleDO {

    /**
     * 主键
     */
    private String id;

    /**
     * 所属系统
     */
    private String system;

    /**
     * 对象名称
     */
    private String modelObj;

    /**
     * 用例规则
     */
    private String caseRule;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 状态标识
     */
    private String status;

    /**
     * 备注
     */
    private String memo;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
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
     * @return property value of caseRule
     */
    public String getCaseRule() {
        return caseRule;
    }

    /**
     *
     *
     * @param caseRule value to be assigned to property caseRule
     */
    public void setCaseRule(String caseRule) {
        this.caseRule = caseRule;
    }

    /**
     *
     *
     * @return property value of priority
     */
    public String getPriority() {
        return priority;
    }

    /**
     *
     *
     * @param priority value to be assigned to property priority
     */
    public void setPriority(String priority) {
        this.priority = priority;
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

    /**
     *
     *
     * @return property value of status
     */
    public String getStatus() {
        return status;
    }

    /**
     *
     *
     * @param status value to be assigned to property status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "CaseRuleDO [id=" + id + ", system=" + system + ", modelObj=" + modelObj
                + ", caseRule=" + caseRule + ", priority=" + priority + ", status=" + status
                + ", memo=" + memo + ", gmtCreate=" + gmtCreate + ", gmtModify=" + gmtModify + "]";
    }

}
