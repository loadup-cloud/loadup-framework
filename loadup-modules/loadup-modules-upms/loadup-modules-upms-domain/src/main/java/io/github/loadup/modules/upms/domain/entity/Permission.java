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
import java.util.List;

/**
 * Permission Entity - Resource permission definition
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class Permission {

    private String id;

    private String parentId;

    private String permissionName;

    private String permissionCode;

    /**
     * Permission type: 1-Menu, 2-Button, 3-API
     */
    private Short permissionType;

    private String resourcePath;

    private String httpMethod;

    private String icon;

    private String componentPath;

    private Integer sortOrder;

    private Boolean visible;

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
    private Permission parent;

    private List<Permission> children;

    /**
     * Check if permission is enabled
     */
    public boolean isEnabled() {
        return status != null && status == 1 && !Boolean.TRUE.equals(deleted);
    }

    /**
     * Check if this is a root permission
     */
    public boolean isRoot() {
        return parentId == null || "0".equals(parentId);
    }

    /**
     * Check if this is a menu permission
     */
    public boolean isMenu() {
        return permissionType != null && permissionType == 1;
    }

    /**
     * Check if this is a button permission
     */
    public boolean isButton() {
        return permissionType != null && permissionType == 2;
    }

    /**
     * Check if this is an API permission
     */
    public boolean isApi() {
        return permissionType != null && permissionType == 3;
    }

    public Permission(String id, String parentId, String permissionName, String permissionCode, Short permissionType, String resourcePath, String httpMethod, String icon, String componentPath, Integer sortOrder, Boolean visible, Short status, Boolean deleted, String remark, String createdBy, LocalDateTime createdTime, String updatedBy, LocalDateTime updatedTime, Permission parent, List<Permission> children) {
        this.id = id;
        this.parentId = parentId;
        this.permissionName = permissionName;
        this.permissionCode = permissionCode;
        this.permissionType = permissionType;
        this.resourcePath = resourcePath;
        this.httpMethod = httpMethod;
        this.icon = icon;
        this.componentPath = componentPath;
        this.sortOrder = sortOrder;
        this.visible = visible;
        this.status = status;
        this.deleted = deleted;
        this.remark = remark;
        this.createdBy = createdBy;
        this.createdTime = createdTime;
        this.updatedBy = updatedBy;
        this.updatedTime = updatedTime;
        this.parent = parent;
        this.children = children;
    }

    public Permission() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public void setPermissionType(Short permissionType) {
        this.permissionType = permissionType;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
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

    public void setParent(Permission parent) {
        this.parent = parent;
    }

    public void setChildren(List<Permission> children) {
        this.children = children;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, parentId, permissionName, permissionCode, permissionType, resourcePath, httpMethod, icon, componentPath, sortOrder, visible, status, deleted, remark, createdBy, createdTime, updatedBy, updatedTime, parent, children);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission other = (Permission) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(parentId, other.parentId)) return false;
        if (!java.util.Objects.equals(permissionName, other.permissionName)) return false;
        if (!java.util.Objects.equals(permissionCode, other.permissionCode)) return false;
        if (!java.util.Objects.equals(permissionType, other.permissionType)) return false;
        if (!java.util.Objects.equals(resourcePath, other.resourcePath)) return false;
        if (!java.util.Objects.equals(httpMethod, other.httpMethod)) return false;
        if (!java.util.Objects.equals(icon, other.icon)) return false;
        if (!java.util.Objects.equals(componentPath, other.componentPath)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        if (!java.util.Objects.equals(visible, other.visible)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(deleted, other.deleted)) return false;
        if (!java.util.Objects.equals(remark, other.remark)) return false;
        if (!java.util.Objects.equals(createdBy, other.createdBy)) return false;
        if (!java.util.Objects.equals(createdTime, other.createdTime)) return false;
        if (!java.util.Objects.equals(updatedBy, other.updatedBy)) return false;
        if (!java.util.Objects.equals(updatedTime, other.updatedTime)) return false;
        if (!java.util.Objects.equals(parent, other.parent)) return false;
        if (!java.util.Objects.equals(children, other.children)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Permission(" + "id=" + id + ", " + "parentId=" + parentId + ", " + "permissionName=" + permissionName + ", " + "permissionCode=" + permissionCode + ", " + "permissionType=" + permissionType + ", " + "resourcePath=" + resourcePath + ", " + "httpMethod=" + httpMethod + ", " + "icon=" + icon + ", " + "componentPath=" + componentPath + ", " + "sortOrder=" + sortOrder + ", " + "visible=" + visible + ", " + "status=" + status + ", " + "deleted=" + deleted + ", " + "remark=" + remark + ", " + "createdBy=" + createdBy + ", " + "createdTime=" + createdTime + ", " + "updatedBy=" + updatedBy + ", " + "updatedTime=" + updatedTime + ", " + "parent=" + parent + ", " + "children=" + children + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String parentId;
        private String permissionName;
        private String permissionCode;
        private Short permissionType;
        private String resourcePath;
        private String httpMethod;
        private String icon;
        private String componentPath;
        private Integer sortOrder;
        private Boolean visible;
        private Short status;
        private Boolean deleted;
        private String remark;
        private String createdBy;
        private LocalDateTime createdTime;
        private String updatedBy;
        private LocalDateTime updatedTime;
        private Permission parent;
        private List<Permission> children;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder permissionName(String permissionName) {
            this.permissionName = permissionName;
            return this;
        }

        public Builder permissionCode(String permissionCode) {
            this.permissionCode = permissionCode;
            return this;
        }

        public Builder permissionType(Short permissionType) {
            this.permissionType = permissionType;
            return this;
        }

        public Builder resourcePath(String resourcePath) {
            this.resourcePath = resourcePath;
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder componentPath(String componentPath) {
            this.componentPath = componentPath;
            return this;
        }

        public Builder sortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder visible(Boolean visible) {
            this.visible = visible;
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

        public Builder parent(Permission parent) {
            this.parent = parent;
            return this;
        }

        public Builder children(List<Permission> children) {
            this.children = children;
            return this;
        }

        public Permission build() {
            return new Permission(this.id, this.parentId, this.permissionName, this.permissionCode, this.permissionType, this.resourcePath, this.httpMethod, this.icon, this.componentPath, this.sortOrder, this.visible, this.status, this.deleted, this.remark, this.createdBy, this.createdTime, this.updatedBy, this.updatedTime, this.parent, this.children);
        }
    }
}
