package io.github.loadup.modules.upms.domain.entity;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Department Entity - Organizational structure with unlimited hierarchy
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class Department {

    private String id;

    private String parentId;

    private String deptName;

    private String deptCode;

    private Integer deptLevel;

    private Integer sortOrder;

    private String leaderUserId;

    private String mobile;

    private String email;

    /**
     * Status: 1-Normal, 0-Disabled
     */
    private Short status;

    private Boolean deleted;

    private String remark;

    private String createdBy;

    private LocalDateTime createdTime;

    private String updatedBy;

    private LocalDateTime updatedTime;

    // Transient fields
    private Department parent;

    private List<Department> children;

    private User leader;

    /**
     * Check if department is enabled
     */
    public boolean isEnabled() {
        return status != null && status == 1 && !Boolean.TRUE.equals(deleted);
    }

    /**
     * Check if this is a root department
     */
    public boolean isRoot() {
        return parentId == null || "0".equals(parentId);
    }

    /**
     * Get full path (for display in tree structure)
     */
    public String getFullPath() {
        if (parent == null || isRoot()) {
            return deptName;
        }
        return parent.getFullPath() + " / " + deptName;
    }

    public Department(String id, String parentId, String deptName, String deptCode, Integer deptLevel, Integer sortOrder, String leaderUserId, String mobile, String email, Short status, Boolean deleted, String remark, String createdBy, LocalDateTime createdTime, String updatedBy, LocalDateTime updatedTime, Department parent, List<Department> children, User leader) {
        this.id = id;
        this.parentId = parentId;
        this.deptName = deptName;
        this.deptCode = deptCode;
        this.deptLevel = deptLevel;
        this.sortOrder = sortOrder;
        this.leaderUserId = leaderUserId;
        this.mobile = mobile;
        this.email = email;
        this.status = status;
        this.deleted = deleted;
        this.remark = remark;
        this.createdBy = createdBy;
        this.createdTime = createdTime;
        this.updatedBy = updatedBy;
        this.updatedTime = updatedTime;
        this.parent = parent;
        this.children = children;
        this.leader = leader;
    }

    public Department() {
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

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public void setParent(Department parent) {
        this.parent = parent;
    }

    public void setChildren(List<Department> children) {
        this.children = children;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, parentId, deptName, deptCode, deptLevel, sortOrder, leaderUserId, mobile, email, status, deleted, remark, createdBy, createdTime, updatedBy, updatedTime, parent, children, leader);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department other = (Department) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(parentId, other.parentId)) return false;
        if (!java.util.Objects.equals(deptName, other.deptName)) return false;
        if (!java.util.Objects.equals(deptCode, other.deptCode)) return false;
        if (!java.util.Objects.equals(deptLevel, other.deptLevel)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        if (!java.util.Objects.equals(leaderUserId, other.leaderUserId)) return false;
        if (!java.util.Objects.equals(mobile, other.mobile)) return false;
        if (!java.util.Objects.equals(email, other.email)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(deleted, other.deleted)) return false;
        if (!java.util.Objects.equals(remark, other.remark)) return false;
        if (!java.util.Objects.equals(createdBy, other.createdBy)) return false;
        if (!java.util.Objects.equals(createdTime, other.createdTime)) return false;
        if (!java.util.Objects.equals(updatedBy, other.updatedBy)) return false;
        if (!java.util.Objects.equals(updatedTime, other.updatedTime)) return false;
        if (!java.util.Objects.equals(parent, other.parent)) return false;
        if (!java.util.Objects.equals(children, other.children)) return false;
        if (!java.util.Objects.equals(leader, other.leader)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Department(" + "id=" + id + ", " + "parentId=" + parentId + ", " + "deptName=" + deptName + ", " + "deptCode=" + deptCode + ", " + "deptLevel=" + deptLevel + ", " + "sortOrder=" + sortOrder + ", " + "leaderUserId=" + leaderUserId + ", " + "mobile=" + mobile + ", " + "email=" + email + ", " + "status=" + status + ", " + "deleted=" + deleted + ", " + "remark=" + remark + ", " + "createdBy=" + createdBy + ", " + "createdTime=" + createdTime + ", " + "updatedBy=" + updatedBy + ", " + "updatedTime=" + updatedTime + ", " + "parent=" + parent + ", " + "children=" + children + ", " + "leader=" + leader + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String parentId;
        private String deptName;
        private String deptCode;
        private Integer deptLevel;
        private Integer sortOrder;
        private String leaderUserId;
        private String mobile;
        private String email;
        private Short status;
        private Boolean deleted;
        private String remark;
        private String createdBy;
        private LocalDateTime createdTime;
        private String updatedBy;
        private LocalDateTime updatedTime;
        private Department parent;
        private List<Department> children;
        private User leader;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder deptName(String deptName) {
            this.deptName = deptName;
            return this;
        }

        public Builder deptCode(String deptCode) {
            this.deptCode = deptCode;
            return this;
        }

        public Builder deptLevel(Integer deptLevel) {
            this.deptLevel = deptLevel;
            return this;
        }

        public Builder sortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder leaderUserId(String leaderUserId) {
            this.leaderUserId = leaderUserId;
            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder status(Short status) {
            this.status = status;
            return this;
        }

        public Builder deleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder updatedTime(LocalDateTime updatedTime) {
            this.updatedTime = updatedTime;
            return this;
        }

        public Builder parent(Department parent) {
            this.parent = parent;
            return this;
        }

        public Builder children(List<Department> children) {
            this.children = children;
            return this;
        }

        public Builder leader(User leader) {
            this.leader = leader;
            return this;
        }

        public Department build() {
            return new Department(this.id, this.parentId, this.deptName, this.deptCode, this.deptLevel, this.sortOrder, this.leaderUserId, this.mobile, this.email, this.status, this.deleted, this.remark, this.createdBy, this.createdTime, this.updatedBy, this.updatedTime, this.parent, this.children, this.leader);
        }
    }
}
