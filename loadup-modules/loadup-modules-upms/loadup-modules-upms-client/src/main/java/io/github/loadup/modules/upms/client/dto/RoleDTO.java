package io.github.loadup.modules.upms.client.dto;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.time.LocalDateTime;
import java.util.List;

/**
 * Role DTO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class RoleDTO {

    private String id;
    private String roleName;
    private String roleCode;
    private String parentId;
    private String parentRoleName;
    private Integer roleLevel;
    private Short dataScope;
    private Integer sortOrder;
    private Short status;
    private List<PermissionDTO> permissions;
    private List<String> departmentIds;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

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
            LocalDateTime createdTime,
            LocalDateTime updatedTime) {
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
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
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

    public String getRemark() {
        return this.remark;
    }

    public LocalDateTime getCreatedTime() {
        return this.createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return this.updatedTime;
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

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String roleName;
        private String roleCode;
        private String parentId;
        private String parentRoleName;
        private Integer roleLevel;
        private Short dataScope;
        private Integer sortOrder;
        private Short status;
        private List<PermissionDTO> permissions;
        private List<String> departmentIds;
        private String remark;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder roleName(String roleName) {
            this.roleName = roleName;
            return this;
        }

        public Builder roleCode(String roleCode) {
            this.roleCode = roleCode;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder parentRoleName(String parentRoleName) {
            this.parentRoleName = parentRoleName;
            return this;
        }

        public Builder roleLevel(Integer roleLevel) {
            this.roleLevel = roleLevel;
            return this;
        }

        public Builder dataScope(Short dataScope) {
            this.dataScope = dataScope;
            return this;
        }

        public Builder sortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder status(Short status) {
            this.status = status;
            return this;
        }

        public Builder permissions(List<PermissionDTO> permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder departmentIds(List<String> departmentIds) {
            this.departmentIds = departmentIds;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder updatedTime(LocalDateTime updatedTime) {
            this.updatedTime = updatedTime;
            return this;
        }

        public RoleDTO build() {
            return new RoleDTO(
                    this.id,
                    this.roleName,
                    this.roleCode,
                    this.parentId,
                    this.parentRoleName,
                    this.roleLevel,
                    this.dataScope,
                    this.sortOrder,
                    this.status,
                    this.permissions,
                    this.departmentIds,
                    this.remark,
                    this.createdTime,
                    this.updatedTime);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
