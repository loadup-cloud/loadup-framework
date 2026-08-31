package io.github.loadup.modules.upms.client.dto;

/*-
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Permission DTO (Tree Node)
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class PermissionDTO {

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
    private List<PermissionDTO> children;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    public PermissionDTO(
            String id,
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
            List<PermissionDTO> children,
            String remark,
            LocalDateTime createdTime,
            LocalDateTime updatedTime) {
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
        this.children = children;
        this.remark = remark;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public PermissionDTO() {}

    public String getId() {
        return this.id;
    }

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

    public List<PermissionDTO> getChildren() {
        return this.children;
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

    public void setChildren(List<PermissionDTO> children) {
        this.children = children;
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
        private List<PermissionDTO> children;
        private String remark;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;

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

        public Builder children(List<PermissionDTO> children) {
            this.children = children;
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

        public PermissionDTO build() {
            return new PermissionDTO(
                    this.id,
                    this.parentId,
                    this.permissionName,
                    this.permissionCode,
                    this.permissionType,
                    this.resourcePath,
                    this.httpMethod,
                    this.icon,
                    this.componentPath,
                    this.sortOrder,
                    this.visible,
                    this.status,
                    this.children,
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
