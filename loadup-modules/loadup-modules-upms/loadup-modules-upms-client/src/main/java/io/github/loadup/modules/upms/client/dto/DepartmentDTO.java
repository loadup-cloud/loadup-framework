package io.github.loadup.modules.upms.client.dto;

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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Department DTO (Tree Node)
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class DepartmentDTO {

    private String id;
    private String parentId;
    private String deptName;
    private String deptCode;
    private Integer deptLevel;
    private Integer sortOrder;
    private String leaderUserId;
    private String leaderUserName;
    private String mobile;
    private String email;
    private Short status;
    private List<DepartmentDTO> children;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    public DepartmentDTO(
            String id,
            String parentId,
            String deptName,
            String deptCode,
            Integer deptLevel,
            Integer sortOrder,
            String leaderUserId,
            String leaderUserName,
            String mobile,
            String email,
            Short status,
            List<DepartmentDTO> children,
            String remark,
            LocalDateTime createdTime,
            LocalDateTime updatedTime) {
        this.id = id;
        this.parentId = parentId;
        this.deptName = deptName;
        this.deptCode = deptCode;
        this.deptLevel = deptLevel;
        this.sortOrder = sortOrder;
        this.leaderUserId = leaderUserId;
        this.leaderUserName = leaderUserName;
        this.mobile = mobile;
        this.email = email;
        this.status = status;
        this.children = children;
        this.remark = remark;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public DepartmentDTO() {}

    public String getId() {
        return this.id;
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

    public String getLeaderUserName() {
        return this.leaderUserName;
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

    public List<DepartmentDTO> getChildren() {
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

    public void setLeaderUserName(String leaderUserName) {
        this.leaderUserName = leaderUserName;
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

    public void setChildren(List<DepartmentDTO> children) {
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
        private String deptName;
        private String deptCode;
        private Integer deptLevel;
        private Integer sortOrder;
        private String leaderUserId;
        private String leaderUserName;
        private String mobile;
        private String email;
        private Short status;
        private List<DepartmentDTO> children;
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

        public Builder leaderUserName(String leaderUserName) {
            this.leaderUserName = leaderUserName;
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

        public Builder children(List<DepartmentDTO> children) {
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

        public DepartmentDTO build() {
            return new DepartmentDTO(
                    this.id,
                    this.parentId,
                    this.deptName,
                    this.deptCode,
                    this.deptLevel,
                    this.sortOrder,
                    this.leaderUserId,
                    this.leaderUserName,
                    this.mobile,
                    this.email,
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
