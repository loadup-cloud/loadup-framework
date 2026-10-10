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
package io.github.loadup.modules.upms.client.dto;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Role DTO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class RoleDTO {

    @Schema(description = "Resource identifier")
    private String id;

    @Schema(description = "Role name")
    private String roleName;

    @Schema(description = "Role code")
    private String roleCode;

    @Schema(description = "Parent id")
    private String parentId;

    @Schema(description = "Parent role name")
    private String parentRoleName;

    @Schema(description = "Role level")
    private Integer roleLevel;

    @Schema(description = "Data scope")
    private Short dataScope;

    @Schema(description = "Display order; smaller values appear first")
    private Integer sortOrder;

    @Schema(description = "Current lifecycle status")
    private Short status;

    @Schema(description = "Permissions")
    private List<PermissionDTO> permissions;

    @Schema(description = "Department ids")
    private List<String> departmentIds;

    @Schema(description = "Children")
    private List<RoleDTO> children;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Creation time in UTC")
    private LocalDateTime createdAt;

    @Schema(description = "Last update time in UTC")
    private LocalDateTime updatedAt;

    public RoleDTO(
            String id,
            String roleName,
            String roleCode,
            String parentId,
            String parentRoleName,
            Integer roleLevel,
            Short dataScope,
            Integer sortOrder,
            Short status,
            List<PermissionDTO> permissions,
            List<String> departmentIds,
            String remark,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.parentId = parentId;
        this.parentRoleName = parentRoleName;
        this.roleLevel = roleLevel;
        this.dataScope = dataScope;
        this.sortOrder = sortOrder;
        this.status = status;
        this.permissions = permissions;
        this.departmentIds = departmentIds;
        this.remark = remark;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public RoleDTO() {}

    public String getId() {
        return this.id;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public String getRoleCode() {
        return this.roleCode;
    }

    public String getParentId() {
        return this.parentId;
    }

    public String getParentRoleName() {
        return this.parentRoleName;
    }

    public Integer getRoleLevel() {
        return this.roleLevel;
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

    public List<PermissionDTO> getPermissions() {
        return this.permissions;
    }

    public List<String> getDepartmentIds() {
        return this.departmentIds;
    }

    public List<RoleDTO> getChildren() {
        return this.children;
    }

    public String getRemark() {
        return this.remark;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setParentRoleName(String parentRoleName) {
        this.parentRoleName = parentRoleName;
    }

    public void setRoleLevel(Integer roleLevel) {
        this.roleLevel = roleLevel;
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

    public void setPermissions(List<PermissionDTO> permissions) {
        this.permissions = permissions;
    }

    public void setDepartmentIds(List<String> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public void setChildren(List<RoleDTO> children) {
        this.children = children;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
