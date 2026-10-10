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

/**
 * Permission Create Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class PermissionCreateCommand {

    @Schema(description = "Parent id")
    private String parentId;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50")
    @Schema(description = "Permission name")
    private String permissionName;

    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100")
    @Pattern(regexp = "^[a-z:]+$", message = "权限编码只能包含小写字母和冒号")
    @Schema(description = "Permission code")
    private String permissionCode;

    /**
     * Permission type: 1-Menu, 2-Button, 3-API
     */
    @Schema(description = "Permission type")
    private Short permissionType;

    @Schema(description = "Resource path")
    private String resourcePath;

    @Schema(description = "Http method")
    private String httpMethod;

    @Schema(description = "Icon")
    private String icon;

    @Schema(description = "Component path")
    private String componentPath;

    @Schema(description = "Display order; smaller values appear first")
    private Integer sortOrder;

    @Schema(description = "Visible")
    private Boolean visible;

    /**
     * Status: 1-Normal, 0-Disabled
     */
    @Schema(description = "Current lifecycle status")
    private Short status;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Created by")
    private String createdBy;

    public PermissionCreateCommand(
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
            String remark,
            String createdBy) {
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
        this.createdBy = createdBy;
    }

    public PermissionCreateCommand() {}

    public String getParentId() {
        return this.parentId;
    }

    public String getPermissionName() {
        return this.permissionName;
    }

    public String getPermissionCode() {
        return this.permissionCode;
    }

    public Short getPermissionType() {
        return this.permissionType;
    }

    public String getResourcePath() {
        return this.resourcePath;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getComponentPath() {
        return this.componentPath;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public Boolean isVisible() {
        return this.visible;
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
