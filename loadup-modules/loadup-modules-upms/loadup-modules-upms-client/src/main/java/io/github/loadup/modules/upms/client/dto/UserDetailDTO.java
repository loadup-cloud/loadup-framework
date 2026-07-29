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

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户详情返回对象")
public class UserDetailDTO implements Serializable {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "账号")
    private String account;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    @Schema(description = "状态：0-正常，1-停用")
    private Integer status;

    @Schema(description = "所属部门ID")
    private String deptId;

    @Schema(description = "所属部门名称")
    private String deptName;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    private List<String> roles;
    private List<String> permissions;

    public UserDetailDTO(String id, String account, String nickname, String realName, String avatar, String mobile, String email, Integer gender, Integer status, String deptId, String deptName, LocalDateTime lastLoginTime, LocalDateTime createdAt, List<String> roles, List<String> permissions) {
        this.id = id;
        this.account = account;
        this.nickname = nickname;
        this.realName = realName;
        this.avatar = avatar;
        this.mobile = mobile;
        this.email = email;
        this.gender = gender;
        this.status = status;
        this.deptId = deptId;
        this.deptName = deptName;
        this.lastLoginTime = lastLoginTime;
        this.createdAt = createdAt;
        this.roles = roles;
        this.permissions = permissions;
    }

    public UserDetailDTO() {
    }

    public String getId() {
        return this.id;
    }

    public String getAccount() {
        return this.account;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getRealName() {
        return this.realName;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getEmail() {
        return this.email;
    }

    public Integer getGender() {
        return this.gender;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getDeptId() {
        return this.deptId;
    }

    public String getDeptName() {
        return this.deptName;
    }

    public LocalDateTime getLastLoginTime() {
        return this.lastLoginTime;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, account, nickname, realName, avatar, mobile, email, gender, status, deptId, deptName, lastLoginTime, createdAt, roles, permissions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailDTO other = (UserDetailDTO) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(account, other.account)) return false;
        if (!java.util.Objects.equals(nickname, other.nickname)) return false;
        if (!java.util.Objects.equals(realName, other.realName)) return false;
        if (!java.util.Objects.equals(avatar, other.avatar)) return false;
        if (!java.util.Objects.equals(mobile, other.mobile)) return false;
        if (!java.util.Objects.equals(email, other.email)) return false;
        if (!java.util.Objects.equals(gender, other.gender)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(deptId, other.deptId)) return false;
        if (!java.util.Objects.equals(deptName, other.deptName)) return false;
        if (!java.util.Objects.equals(lastLoginTime, other.lastLoginTime)) return false;
        if (!java.util.Objects.equals(createdAt, other.createdAt)) return false;
        if (!java.util.Objects.equals(roles, other.roles)) return false;
        if (!java.util.Objects.equals(permissions, other.permissions)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UserDetailDTO(" + "id=" + id + ", " + "account=" + account + ", " + "nickname=" + nickname + ", " + "realName=" + realName + ", " + "avatar=" + avatar + ", " + "mobile=" + mobile + ", " + "email=" + email + ", " + "gender=" + gender + ", " + "status=" + status + ", " + "deptId=" + deptId + ", " + "deptName=" + deptName + ", " + "lastLoginTime=" + lastLoginTime + ", " + "createdAt=" + createdAt + ", " + "roles=" + roles + ", " + "permissions=" + permissions + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String account;
        private String nickname;
        private String realName;
        private String avatar;
        private String mobile;
        private String email;
        private Integer gender;
        private Integer status;
        private String deptId;
        private String deptName;
        private LocalDateTime lastLoginTime;
        private LocalDateTime createdAt;
        private List<String> roles;
        private List<String> permissions;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder account(String account) {
            this.account = account;
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

        public Builder avatar(String avatar) {
            this.avatar = avatar;
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

        public Builder gender(Integer gender) {
            this.gender = gender;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder deptId(String deptId) {
            this.deptId = deptId;
            return this;
        }

        public Builder deptName(String deptName) {
            this.deptName = deptName;
            return this;
        }

        public Builder lastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder permissions(List<String> permissions) {
            this.permissions = permissions;
            return this;
        }

        public UserDetailDTO build() {
            return new UserDetailDTO(this.id, this.account, this.nickname, this.realName, this.avatar, this.mobile, this.email, this.gender, this.status, this.deptId, this.deptName, this.lastLoginTime, this.createdAt, this.roles, this.permissions);
        }
    }
}
