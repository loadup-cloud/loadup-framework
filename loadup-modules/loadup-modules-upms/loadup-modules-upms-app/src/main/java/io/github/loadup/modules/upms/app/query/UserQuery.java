package io.github.loadup.modules.upms.app.query;

/*-
 * #%L
 * Loadup Modules UPMS App Layer
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

/**
 * User Query Parameters
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class UserQuery {

    private String username;
    private String nickname;
    private String realName;
    private String email;
    private String mobile;
    private String deptId;
    private Short status;
    private Boolean deleted;

    // Pagination
    private Integer page = 1;
    private Integer size = 20;
    private String sortBy = "createdTime";
    private String sortOrder = "DESC";

    public UserQuery(
            String username,
            String nickname,
            String realName,
            String email,
            String mobile,
            String deptId,
            Short status,
            Boolean deleted,
            Integer page,
            Integer size,
            String sortBy,
            String sortOrder) {
        this.username = username;
        this.nickname = nickname;
        this.realName = realName;
        this.email = email;
        this.mobile = mobile;
        this.deptId = deptId;
        this.status = status;
        this.deleted = deleted;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
    }

    public UserQuery() {}

    public String getUsername() {
        return this.username;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getRealName() {
        return this.realName;
    }

    public String getEmail() {
        return this.email;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getDeptId() {
        return this.deptId;
    }

    public Short getStatus() {
        return this.status;
    }

    public Boolean isDeleted() {
        return this.deleted;
    }

    public Integer getPage() {
        return this.page;
    }

    public Integer getSize() {
        return this.size;
    }

    public String getSortBy() {
        return this.sortBy;
    }

    public String getSortOrder() {
        return this.sortOrder;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                username, nickname, realName, email, mobile, deptId, status, deleted, page, size, sortBy, sortOrder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserQuery other = (UserQuery) o;
        if (!java.util.Objects.equals(username, other.username)) return false;
        if (!java.util.Objects.equals(nickname, other.nickname)) return false;
        if (!java.util.Objects.equals(realName, other.realName)) return false;
        if (!java.util.Objects.equals(email, other.email)) return false;
        if (!java.util.Objects.equals(mobile, other.mobile)) return false;
        if (!java.util.Objects.equals(deptId, other.deptId)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(deleted, other.deleted)) return false;
        if (!java.util.Objects.equals(page, other.page)) return false;
        if (!java.util.Objects.equals(size, other.size)) return false;
        if (!java.util.Objects.equals(sortBy, other.sortBy)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UserQuery(" + "username=" + username + ", " + "nickname=" + nickname + ", " + "realName=" + realName
                + ", " + "email=" + email + ", " + "mobile=" + mobile + ", " + "deptId=" + deptId + ", " + "status="
                + status + ", " + "deleted=" + deleted + ", " + "page=" + page + ", " + "size=" + size + ", "
                + "sortBy=" + sortBy + ", " + "sortOrder=" + sortOrder + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String username;
        private String nickname;
        private String realName;
        private String email;
        private String mobile;
        private String deptId;
        private Short status;
        private Boolean deleted;
        private Integer page = 1;
        private Integer size = 20;
        private String sortBy = "createdTime";
        private String sortOrder = "DESC";

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder realName(String realName) {
            this.realName = realName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder deptId(String deptId) {
            this.deptId = deptId;
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

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder sortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public UserQuery build() {
            return new UserQuery(
                    this.username,
                    this.nickname,
                    this.realName,
                    this.email,
                    this.mobile,
                    this.deptId,
                    this.status,
                    this.deleted,
                    this.page,
                    this.size,
                    this.sortBy,
                    this.sortOrder);
        }
    }
}
