package io.github.loadup.modules.upms.client.command;

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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Role Update Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class RoleUpdateCommand {

    @NotNull(message = "角色ID不能为空")
    private String id;

    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;

    private String parentId;

    /**
     * Data scope: 1-All, 2-Custom, 3-Dept, 4-Dept and children, 5-Self only
     */
    private Short dataScope;

    private Integer sortOrder;

    private Short status;

    private List<String> permissionIds;

    private List<String> departmentIds;

    private String remark;

    private String updatedBy;

    public RoleUpdateCommand(
            String id,
            String roleName,
            String parentId,
            Short dataScope,
            Integer sortOrder,
            Short status,
            List<String> permissionIds,
            List<String> departmentIds,
            String remark,
            String updatedBy) {
        this.id = id;
        this.roleName = roleName;
        this.parentId = parentId;
        this.dataScope = dataScope;
        this.sortOrder = sortOrder;
        this.status = status;
        this.permissionIds = permissionIds;
        this.departmentIds = departmentIds;
        this.remark = remark;
        this.updatedBy = updatedBy;
    }

    public RoleUpdateCommand() {}

    public String getId() {
        return this.id;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public String getParentId() {
        return this.parentId;
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

    public List<String> getPermissionIds() {
        return this.permissionIds;
    }

    public List<String> getDepartmentIds() {
        return this.departmentIds;
    }

    public String getRemark() {
        return this.remark;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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

    public void setPermissionIds(List<String> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public void setDepartmentIds(List<String> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                id, roleName, parentId, dataScope, sortOrder, status, permissionIds, departmentIds, remark, updatedBy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleUpdateCommand other = (RoleUpdateCommand) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(roleName, other.roleName)) return false;
        if (!java.util.Objects.equals(parentId, other.parentId)) return false;
        if (!java.util.Objects.equals(dataScope, other.dataScope)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(permissionIds, other.permissionIds)) return false;
        if (!java.util.Objects.equals(departmentIds, other.departmentIds)) return false;
        if (!java.util.Objects.equals(remark, other.remark)) return false;
        if (!java.util.Objects.equals(updatedBy, other.updatedBy)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "RoleUpdateCommand(" + "id=" + id + ", " + "roleName=" + roleName + ", " + "parentId=" + parentId + ", "
                + "dataScope=" + dataScope + ", " + "sortOrder=" + sortOrder + ", " + "status=" + status + ", "
                + "permissionIds=" + permissionIds + ", " + "departmentIds=" + departmentIds + ", " + "remark=" + remark
                + ", " + "updatedBy=" + updatedBy + ")";
    }
}
