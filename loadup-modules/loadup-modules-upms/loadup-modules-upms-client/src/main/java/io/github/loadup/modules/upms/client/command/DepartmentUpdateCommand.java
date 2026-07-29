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

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Department Update Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class DepartmentUpdateCommand {

    @NotNull(message = "部门ID不能为空")
    private String id;

    private String parentId;

    @Size(max = 50, message = "部门名称长度不能超过50")
    private String deptName;

    private Integer sortOrder;

    private String leaderUserId;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @Email(message = "邮箱格式不正确")
    private String email;

    private Short status;

    private String remark;

    private String updatedBy;

    public DepartmentUpdateCommand(
            String id,
            String parentId,
            String deptName,
            Integer sortOrder,
            String leaderUserId,
            String mobile,
            String email,
            Short status,
            String remark,
            String updatedBy) {
        this.id = id;
        this.parentId = parentId;
        this.deptName = deptName;
        this.sortOrder = sortOrder;
        this.leaderUserId = leaderUserId;
        this.mobile = mobile;
        this.email = email;
        this.status = status;
        this.remark = remark;
        this.updatedBy = updatedBy;
    }

    public DepartmentUpdateCommand() {}

    public String getId() {
        return this.id;
    }

    public String getParentId() {
        return this.parentId;
    }

    public String getDeptName() {
        return this.deptName;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public String getLeaderUserId() {
        return this.leaderUserId;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getEmail() {
        return this.email;
    }

    public Short getStatus() {
        return this.status;
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

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setLeaderUserId(String leaderUserId) {
        this.leaderUserId = leaderUserId;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(Short status) {
        this.status = status;
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
                id, parentId, deptName, sortOrder, leaderUserId, mobile, email, status, remark, updatedBy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepartmentUpdateCommand other = (DepartmentUpdateCommand) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(parentId, other.parentId)) return false;
        if (!java.util.Objects.equals(deptName, other.deptName)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        if (!java.util.Objects.equals(leaderUserId, other.leaderUserId)) return false;
        if (!java.util.Objects.equals(mobile, other.mobile)) return false;
        if (!java.util.Objects.equals(email, other.email)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(remark, other.remark)) return false;
        if (!java.util.Objects.equals(updatedBy, other.updatedBy)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "DepartmentUpdateCommand(" + "id=" + id + ", " + "parentId=" + parentId + ", " + "deptName=" + deptName
                + ", " + "sortOrder=" + sortOrder + ", " + "leaderUserId=" + leaderUserId + ", " + "mobile=" + mobile
                + ", " + "email=" + email + ", " + "status=" + status + ", " + "remark=" + remark + ", " + "updatedBy="
                + updatedBy + ")";
    }
}
