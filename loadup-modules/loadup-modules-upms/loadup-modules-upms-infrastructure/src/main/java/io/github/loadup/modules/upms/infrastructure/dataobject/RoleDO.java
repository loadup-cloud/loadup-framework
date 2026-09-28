package io.github.loadup.modules.upms.infrastructure.dataobject;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;

/**
 * Role Data Object
 */
@Table("upms_role")
public class RoleDO extends BaseDO {

    private String roleName;

    private String roleCode;

    @Column("parent_role_id")
    private String parentId;

    private Integer roleLevel;

    private Short dataScope;

    private Integer sortOrder;

    private Short status;

    private String remark;

    private String createdBy;

    private String updatedBy;

    public RoleDO(
            String roleName,
            String roleCode,
            String parentId,
            Integer roleLevel,
            Short dataScope,
            Integer sortOrder,
            Short status,
            String remark) {
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.parentId = parentId;
        this.roleLevel = roleLevel;
        this.dataScope = dataScope;
        this.sortOrder = sortOrder;
        this.status = status;
        this.remark = remark;
    }

    public RoleDO() {}

    public String getRoleName() {
        return this.roleName;
    }

    public String getRoleCode() {
        return this.roleCode;
    }

    public String getParentId() {
        return this.parentId;
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

    public String getRemark() {
        return this.remark;
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
