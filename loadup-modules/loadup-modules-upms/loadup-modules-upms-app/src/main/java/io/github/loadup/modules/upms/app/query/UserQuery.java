package io.github.loadup.modules.upms.app.query;

/*-
 * #%L
 * Loadup Modules UPMS App Layer
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

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
