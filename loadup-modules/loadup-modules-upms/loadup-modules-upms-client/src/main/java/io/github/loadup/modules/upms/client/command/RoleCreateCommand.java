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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Role Create Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class RoleCreateCommand {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50")
    @Schema(description = "Role name")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50")
    @Pattern(regexp = "^[A-Z_]+$", message = "角色编码只能包含大写字母和下划线")
    @Schema(description = "Role code")
    private String roleCode;

    /**
     * Parent role ID for role inheritance (RBAC3 feature)
     */
    @Schema(description = "Parent id")
    private String parentId;

    /**
     * Data scope: 1-All, 2-Custom, 3-Dept, 4-Dept and children, 5-Self only
     */
    @Schema(description = "Data scope")
    private Short dataScope;

    @Schema(description = "Display order; smaller values appear first")
    private Integer sortOrder;

    /**
     * Status: 1-Normal, 0-Disabled
     */
    @Schema(description = "Current lifecycle status")
    private Short status;

    @Schema(description = "Permission ids")
    private List<String> permissionIds;

    @Schema(description = "Department ids")
    private List<String> departmentIds; // For custom data scope

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Created by")
    private String createdBy;

    public RoleCreateCommand(
            String roleName,
            String roleCode,
            String parentId,
            Short dataScope,
            Integer sortOrder,
            Short status,
            List<String> permissionIds,
            List<String> departmentIds,
            String remark,
            String createdBy) {
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.parentId = parentId;
        this.dataScope = dataScope;
        this.sortOrder = sortOrder;
        this.status = status;
        this.permissionIds = permissionIds;
        this.departmentIds = departmentIds;
        this.remark = remark;
        this.createdBy = createdBy;
    }

    public RoleCreateCommand() {}

    public String getRoleName() {
        return this.roleName;
    }

    public String getRoleCode() {
        return this.roleCode;
    }

    public String getParentId() {
        return this.parentId;
    }

    public Short getDataScope() {
        return this.dataScope;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public Short getStatus() {
        return this.status;
    }

    public List<String> getPermissionIds() {
        return this.permissionIds;
    }

    public List<String> getDepartmentIds() {
        return this.departmentIds;
    }

    public String getRemark() {
        return this.remark;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setDataScope(Short dataScope) {
        this.dataScope = dataScope;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public void setPermissionIds(List<String> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public void setDepartmentIds(List<String> departmentIds) {
        this.departmentIds = departmentIds;
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
