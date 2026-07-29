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
 * Department Data Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Table("upms_department")
public class DepartmentDO extends BaseDO {

    private String parentId;

    private String deptName;

    private String deptCode;

    private Integer deptLevel;

    private Integer sortOrder;

    private String leaderUserId;

    private String mobile;

    private String email;

    private Short status;

    private String remark;

    public DepartmentDO(String parentId, String deptName, String deptCode, Integer deptLevel, Integer sortOrder, String leaderUserId, String mobile, String email, Short status, String remark) {
        this.parentId = parentId;
        this.deptName = deptName;
        this.deptCode = deptCode;
        this.deptLevel = deptLevel;
        this.sortOrder = sortOrder;
        this.leaderUserId = leaderUserId;
        this.mobile = mobile;
        this.email = email;
        this.status = status;
        this.remark = remark;
    }

    public DepartmentDO() {
    }

    public String getParentId() {
        return this.parentId;
    }

    public String getDeptName() {
        return this.deptName;
    }

    public String getDeptCode() {
        return this.deptCode;
    }

    public Integer getDeptLevel() {
        return this.deptLevel;
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

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public void setDeptLevel(Integer deptLevel) {
        this.deptLevel = deptLevel;
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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), parentId, deptName, deptCode, deptLevel, sortOrder, leaderUserId, mobile, email, status, remark);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DepartmentDO other = (DepartmentDO) o;
        if (!java.util.Objects.equals(parentId, other.parentId)) return false;
        if (!java.util.Objects.equals(deptName, other.deptName)) return false;
        if (!java.util.Objects.equals(deptCode, other.deptCode)) return false;
        if (!java.util.Objects.equals(deptLevel, other.deptLevel)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        if (!java.util.Objects.equals(leaderUserId, other.leaderUserId)) return false;
        if (!java.util.Objects.equals(mobile, other.mobile)) return false;
        if (!java.util.Objects.equals(email, other.email)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(remark, other.remark)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "DepartmentDO(" + "super=" + super.toString() + ", " + "parentId=" + parentId + ", " + "deptName=" + deptName + ", " + "deptCode=" + deptCode + ", " + "deptLevel=" + deptLevel + ", " + "sortOrder=" + sortOrder + ", " + "leaderUserId=" + leaderUserId + ", " + "mobile=" + mobile + ", " + "email=" + email + ", " + "status=" + status + ", " + "remark=" + remark + ")";
    }
}
