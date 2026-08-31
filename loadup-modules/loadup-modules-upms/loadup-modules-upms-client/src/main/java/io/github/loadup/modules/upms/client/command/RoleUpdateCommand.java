package io.github.loadup.modules.upms.client.command;

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
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
