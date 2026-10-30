/*
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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
package io.github.loadup.modules.upms.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;

/**
 * Permission Data Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Table("upms_permission")
public class PermissionDO extends BaseDO {

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

    private String remark;

    private String createdBy;

    private String updatedBy;

    public PermissionDO(
            String parentId,
            String permissionName,
            String permissionCode,
            Short permissionType,
            String resourcePath,
            String httpMethod,
            String icon,
            String componentPath,
            Integer sortOrder,
            Boolean visible,
            Short status,
            String remark) {
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
        this.remark = remark;
    }

    public PermissionDO() {}

    public String getParentId() {
        return this.parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPermissionName() {
        return this.permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionCode() {
        return this.permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public Short getPermissionType() {
        return this.permissionType;
    }

    public void setPermissionType(Short permissionType) {
        this.permissionType = permissionType;
    }

    public String getResourcePath() {
        return this.resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getComponentPath() {
        return this.componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getVisible() {
        return this.visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Short getStatus() {
        return this.status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getRemark() {
        return this.remark;
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

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
