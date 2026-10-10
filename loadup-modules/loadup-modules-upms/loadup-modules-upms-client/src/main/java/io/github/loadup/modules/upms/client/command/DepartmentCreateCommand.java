/*
 * #%L
 * Loadup Modules UPMS Client Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.modules.upms.client.command;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Department Create Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class DepartmentCreateCommand {

    @Schema(description = "Parent id")
    private String parentId;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称长度不能超过50")
    @Schema(description = "Dept name")
    private String deptName;

    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码长度不能超过50")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "部门编码只能包含大写字母、数字和下划线")
    @Schema(description = "Dept code")
    private String deptCode;

    @Schema(description = "Display order; smaller values appear first")
    private Integer sortOrder;

    @Schema(description = "Leader user id")
    private String leaderUserId;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "Mobile")
    private String mobile;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "Email")
    private String email;

    /**
     * Status: 1-Normal, 0-Disabled
     */
    @Schema(description = "Current lifecycle status")
    private Short status;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Created by")
    private String createdBy;

    public DepartmentCreateCommand(
            String parentId,
            String deptName,
            String deptCode,
            Integer sortOrder,
            String leaderUserId,
            String mobile,
            String email,
            Short status,
            String remark,
            String createdBy) {
        this.parentId = parentId;
        this.deptName = deptName;
        this.deptCode = deptCode;
        this.sortOrder = sortOrder;
        this.leaderUserId = leaderUserId;
        this.mobile = mobile;
        this.email = email;
        this.status = status;
        this.remark = remark;
        this.createdBy = createdBy;
    }

    public DepartmentCreateCommand() {}

    public String getParentId() {
        return this.parentId;
    }

    public String getDeptName() {
        return this.deptName;
    }

    public String getDeptCode() {
        return this.deptCode;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public String getLeaderUserId() {
        return this.leaderUserId;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getEmail() {
        return this.email;
    }

    public Short getStatus() {
        return this.status;
    }

    public String getRemark() {
        return this.remark;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setLeaderUserId(String leaderUserId) {
        this.leaderUserId = leaderUserId;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
