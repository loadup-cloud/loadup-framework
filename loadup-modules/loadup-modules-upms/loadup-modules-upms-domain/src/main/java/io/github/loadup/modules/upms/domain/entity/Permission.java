/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
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
package io.github.loadup.modules.upms.domain.entity;

import io.github.loadup.commons.domain.BaseEntity;
import java.time.LocalDateTime;

/**
 * Permission Entity - Resource permission definition
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class Permission extends BaseEntity {

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

    private boolean visible;

    /**
     * Status: 1-Normal, 0-Disabled
     */
    private Short status;

    private Boolean deleted;

    private String remark;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public Short getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(Short permissionType) {
        this.permissionType = permissionType;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
