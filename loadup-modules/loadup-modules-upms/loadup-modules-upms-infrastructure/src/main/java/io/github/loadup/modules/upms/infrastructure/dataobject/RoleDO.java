package io.github.loadup.modules.upms.infrastructure.dataobject;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;

/**
 * Role Data Object
 */
@Table("upms_role")
public class RoleDO extends BaseDO {

    private String roleName;

    private String roleCode;

    private String parentId;

    private Integer roleLevel;

    private Short dataScope;

    private Integer sortOrder;

    private Short status;

    private String remark;

    public RoleDO(String roleName, String roleCode, String parentId, Integer roleLevel, Short dataScope, Integer sortOrder, Short status, String remark) {
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.parentId = parentId;
        this.roleLevel = roleLevel;
        this.dataScope = dataScope;
        this.sortOrder = sortOrder;
        this.status = status;
        this.remark = remark;
    }

    public RoleDO() {
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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), roleName, roleCode, parentId, roleLevel, dataScope, sortOrder, status, remark);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RoleDO other = (RoleDO) o;
        if (!java.util.Objects.equals(roleName, other.roleName)) return false;
        if (!java.util.Objects.equals(roleCode, other.roleCode)) return false;
        if (!java.util.Objects.equals(parentId, other.parentId)) return false;
        if (!java.util.Objects.equals(roleLevel, other.roleLevel)) return false;
        if (!java.util.Objects.equals(dataScope, other.dataScope)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(remark, other.remark)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "RoleDO(" + "super=" + super.toString() + ", " + "roleName=" + roleName + ", " + "roleCode=" + roleCode + ", " + "parentId=" + parentId + ", " + "roleLevel=" + roleLevel + ", " + "dataScope=" + dataScope + ", " + "sortOrder=" + sortOrder + ", " + "status=" + status + ", " + "remark=" + remark + ")";
    }
}
