package io.github.loadup.modules.upms.domain.entity;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
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
import java.util.ArrayList;
import java.util.List;

/**
 * Role Entity - RBAC3 Role with hierarchy support
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class Role {

    private String id;

    private String roleName;

    private String roleCode;

    /**
     * Parent role ID for role inheritance (RBAC3 feature)
     */
    private String parentId;

    private Integer roleLevel;

    /**
     * Data scope: 1-All, 2-Custom, 3-Dept, 4-Dept and children, 5-Self only
     */
    private Short dataScope;

    private Integer sortOrder;

    /**
     * Status: 1-Normal, 0-Disabled
     */
    private Short status;

    private Boolean deleted;

    private String remark;

    private String createdBy;

    private LocalDateTime createdTime;

    private String updatedBy;

    private LocalDateTime updatedTime;

    // Transient fields
    private Role parentRole;

    private List<Role> childRoles;

    private List<Permission> permissions;

    private List<Department> departments;

    /**
     * Check if role is enabled
     */
    public boolean isEnabled() {
        return status != null && status == 1 && !Boolean.TRUE.equals(deleted);
    }

    /**
     * Check if this is a root role (no parent)
     */
    public boolean isRoot() {
        return parentId == null;
    }

    /**
     * Get all inherited permissions (including parent roles)
     */
    public List<Permission> getAllInheritedPermissions() {
        List<Permission> allPermissions = new ArrayList<>();
        if (permissions != null) {
            allPermissions.addAll(permissions);
        }

        // Recursively add parent role permissions
        if (parentRole != null && parentRole.isEnabled()) {
            allPermissions.addAll(parentRole.getAllInheritedPermissions());
        }

        return allPermissions;
    }

    public Role(String id, String roleName, String roleCode, String parentId, Integer roleLevel, Short dataScope, Integer sortOrder, Short status, Boolean deleted, String remark, String createdBy, LocalDateTime createdTime, String updatedBy, LocalDateTime updatedTime, Role parentRole, List<Role> childRoles, List<Permission> permissions, List<Department> departments) {
        this.id = id;
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.parentId = parentId;
        this.roleLevel = roleLevel;
        this.dataScope = dataScope;
        this.sortOrder = sortOrder;
        this.status = status;
        this.deleted = deleted;
        this.remark = remark;
        this.createdBy = createdBy;
        this.createdTime = createdTime;
        this.updatedBy = updatedBy;
        this.updatedTime = updatedTime;
        this.parentRole = parentRole;
        this.childRoles = childRoles;
        this.permissions = permissions;
        this.departments = departments;
    }

    public Role() {
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

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public void setParentRole(Role parentRole) {
        this.parentRole = parentRole;
    }

    public void setChildRoles(List<Role> childRoles) {
        this.childRoles = childRoles;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, roleName, roleCode, parentId, roleLevel, dataScope, sortOrder, status, deleted, remark, createdBy, createdTime, updatedBy, updatedTime, parentRole, childRoles, permissions, departments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role other = (Role) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(roleName, other.roleName)) return false;
        if (!java.util.Objects.equals(roleCode, other.roleCode)) return false;
        if (!java.util.Objects.equals(parentId, other.parentId)) return false;
        if (!java.util.Objects.equals(roleLevel, other.roleLevel)) return false;
        if (!java.util.Objects.equals(dataScope, other.dataScope)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(deleted, other.deleted)) return false;
        if (!java.util.Objects.equals(remark, other.remark)) return false;
        if (!java.util.Objects.equals(createdBy, other.createdBy)) return false;
        if (!java.util.Objects.equals(createdTime, other.createdTime)) return false;
        if (!java.util.Objects.equals(updatedBy, other.updatedBy)) return false;
        if (!java.util.Objects.equals(updatedTime, other.updatedTime)) return false;
        if (!java.util.Objects.equals(parentRole, other.parentRole)) return false;
        if (!java.util.Objects.equals(childRoles, other.childRoles)) return false;
        if (!java.util.Objects.equals(permissions, other.permissions)) return false;
        if (!java.util.Objects.equals(departments, other.departments)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Role(" + "id=" + id + ", " + "roleName=" + roleName + ", " + "roleCode=" + roleCode + ", " + "parentId=" + parentId + ", " + "roleLevel=" + roleLevel + ", " + "dataScope=" + dataScope + ", " + "sortOrder=" + sortOrder + ", " + "status=" + status + ", " + "deleted=" + deleted + ", " + "remark=" + remark + ", " + "createdBy=" + createdBy + ", " + "createdTime=" + createdTime + ", " + "updatedBy=" + updatedBy + ", " + "updatedTime=" + updatedTime + ", " + "parentRole=" + parentRole + ", " + "childRoles=" + childRoles + ", " + "permissions=" + permissions + ", " + "departments=" + departments + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String roleName;
        private String roleCode;
        private String parentId;
        private Integer roleLevel;
        private Short dataScope;
        private Integer sortOrder;
        private Short status;
        private Boolean deleted;
        private String remark;
        private String createdBy;
        private LocalDateTime createdTime;
        private String updatedBy;
        private LocalDateTime updatedTime;
        private Role parentRole;
        private List<Role> childRoles;
        private List<Permission> permissions;
        private List<Department> departments;

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

        public Builder deleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder updatedTime(LocalDateTime updatedTime) {
            this.updatedTime = updatedTime;
            return this;
        }

        public Builder parentRole(Role parentRole) {
            this.parentRole = parentRole;
            return this;
        }

        public Builder childRoles(List<Role> childRoles) {
            this.childRoles = childRoles;
            return this;
        }

        public Builder permissions(List<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder departments(List<Department> departments) {
            this.departments = departments;
            return this;
        }

        public Role build() {
            return new Role(this.id, this.roleName, this.roleCode, this.parentId, this.roleLevel, this.dataScope, this.sortOrder, this.status, this.deleted, this.remark, this.createdBy, this.createdTime, this.updatedBy, this.updatedTime, this.parentRole, this.childRoles, this.permissions, this.departments);
        }
    }
}
