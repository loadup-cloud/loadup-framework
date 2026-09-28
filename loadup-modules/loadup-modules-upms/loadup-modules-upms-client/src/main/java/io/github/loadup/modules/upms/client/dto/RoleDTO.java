package io.github.loadup.modules.upms.client.dto;

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
    private List<RoleDTO> children;
    private String remark;
    private LocalDateTime createdAt;
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
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

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

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
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
                    this.createdAt,
                    this.updatedAt);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
