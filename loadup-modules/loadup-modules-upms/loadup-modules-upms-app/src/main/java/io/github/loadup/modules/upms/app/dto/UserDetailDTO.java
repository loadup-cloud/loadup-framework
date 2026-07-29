package io.github.loadup.modules.upms.app.dto;

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

import io.github.loadup.modules.upms.client.dto.RoleDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User Detail DTO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class UserDetailDTO {

    private String id;
    private String username;
    private String nickname;
    private String realName;
    private String deptId;
    private String deptName;
    private String email;
    private Boolean emailVerified;
    private String mobile;
    private Boolean mobileVerified;
    private String avatar;
    private Short gender;
    private LocalDate birthday;
    private Short status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private List<RoleDTO> roles;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    public UserDetailDTO(
            String id,
            String username,
            String nickname,
            String realName,
            String deptId,
            String deptName,
            String email,
            Boolean emailVerified,
            String mobile,
            Boolean mobileVerified,
            String avatar,
            Short gender,
            LocalDate birthday,
            Short status,
            LocalDateTime lastLoginTime,
            String lastLoginIp,
            List<RoleDTO> roles,
            String remark,
            LocalDateTime createdTime,
            LocalDateTime updatedTime) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.realName = realName;
        this.deptId = deptId;
        this.deptName = deptName;
        this.email = email;
        this.emailVerified = emailVerified;
        this.mobile = mobile;
        this.mobileVerified = mobileVerified;
        this.avatar = avatar;
        this.gender = gender;
        this.birthday = birthday;
        this.status = status;
        this.lastLoginTime = lastLoginTime;
        this.lastLoginIp = lastLoginIp;
        this.roles = roles;
        this.remark = remark;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public UserDetailDTO() {}

    public String getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getRealName() {
        return this.realName;
    }

    public String getDeptId() {
        return this.deptId;
    }

    public String getDeptName() {
        return this.deptName;
    }

    public String getEmail() {
        return this.email;
    }

    public Boolean isEmailVerified() {
        return this.emailVerified;
    }

    public String getMobile() {
        return this.mobile;
    }

    public Boolean isMobileVerified() {
        return this.mobileVerified;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public Short getGender() {
        return this.gender;
    }

    public LocalDate getBirthday() {
        return this.birthday;
    }

    public Short getStatus() {
        return this.status;
    }

    public LocalDateTime getLastLoginTime() {
        return this.lastLoginTime;
    }

    public String getLastLoginIp() {
        return this.lastLoginIp;
    }

    public List<RoleDTO> getRoles() {
        return this.roles;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setMobileVerified(Boolean mobileVerified) {
        this.mobileVerified = mobileVerified;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setGender(Short gender) {
        this.gender = gender;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                id,
                username,
                nickname,
                realName,
                deptId,
                deptName,
                email,
                emailVerified,
                mobile,
                mobileVerified,
                avatar,
                gender,
                birthday,
                status,
                lastLoginTime,
                lastLoginIp,
                roles,
                remark,
                createdTime,
                updatedTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailDTO other = (UserDetailDTO) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(username, other.username)) return false;
        if (!java.util.Objects.equals(nickname, other.nickname)) return false;
        if (!java.util.Objects.equals(realName, other.realName)) return false;
        if (!java.util.Objects.equals(deptId, other.deptId)) return false;
        if (!java.util.Objects.equals(deptName, other.deptName)) return false;
        if (!java.util.Objects.equals(email, other.email)) return false;
        if (!java.util.Objects.equals(emailVerified, other.emailVerified)) return false;
        if (!java.util.Objects.equals(mobile, other.mobile)) return false;
        if (!java.util.Objects.equals(mobileVerified, other.mobileVerified)) return false;
        if (!java.util.Objects.equals(avatar, other.avatar)) return false;
        if (!java.util.Objects.equals(gender, other.gender)) return false;
        if (!java.util.Objects.equals(birthday, other.birthday)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(lastLoginTime, other.lastLoginTime)) return false;
        if (!java.util.Objects.equals(lastLoginIp, other.lastLoginIp)) return false;
        if (!java.util.Objects.equals(roles, other.roles)) return false;
        if (!java.util.Objects.equals(remark, other.remark)) return false;
        if (!java.util.Objects.equals(createdTime, other.createdTime)) return false;
        if (!java.util.Objects.equals(updatedTime, other.updatedTime)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UserDetailDTO(" + "id=" + id + ", " + "username=" + username + ", " + "nickname=" + nickname + ", "
                + "realName=" + realName + ", " + "deptId=" + deptId + ", " + "deptName=" + deptName + ", " + "email="
                + email + ", " + "emailVerified=" + emailVerified + ", " + "mobile=" + mobile + ", " + "mobileVerified="
                + mobileVerified + ", " + "avatar=" + avatar + ", " + "gender=" + gender + ", " + "birthday=" + birthday
                + ", " + "status=" + status + ", " + "lastLoginTime=" + lastLoginTime + ", " + "lastLoginIp="
                + lastLoginIp + ", " + "roles=" + roles + ", " + "remark=" + remark + ", " + "createdTime="
                + createdTime + ", " + "updatedTime=" + updatedTime + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String username;
        private String nickname;
        private String realName;
        private String deptId;
        private String deptName;
        private String email;
        private Boolean emailVerified;
        private String mobile;
        private Boolean mobileVerified;
        private String avatar;
        private Short gender;
        private LocalDate birthday;
        private Short status;
        private LocalDateTime lastLoginTime;
        private String lastLoginIp;
        private List<RoleDTO> roles;
        private String remark;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

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

        public Builder deptId(String deptId) {
            this.deptId = deptId;
            return this;
        }

        public Builder deptName(String deptName) {
            this.deptName = deptName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder emailVerified(Boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder mobileVerified(Boolean mobileVerified) {
            this.mobileVerified = mobileVerified;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder gender(Short gender) {
            this.gender = gender;
            return this;
        }

        public Builder birthday(LocalDate birthday) {
            this.birthday = birthday;
            return this;
        }

        public Builder status(Short status) {
            this.status = status;
            return this;
        }

        public Builder lastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
            return this;
        }

        public Builder lastLoginIp(String lastLoginIp) {
            this.lastLoginIp = lastLoginIp;
            return this;
        }

        public Builder roles(List<RoleDTO> roles) {
            this.roles = roles;
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

        public UserDetailDTO build() {
            return new UserDetailDTO(
                    this.id,
                    this.username,
                    this.nickname,
                    this.realName,
                    this.deptId,
                    this.deptName,
                    this.email,
                    this.emailVerified,
                    this.mobile,
                    this.mobileVerified,
                    this.avatar,
                    this.gender,
                    this.birthday,
                    this.status,
                    this.lastLoginTime,
                    this.lastLoginIp,
                    this.roles,
                    this.remark,
                    this.createdTime,
                    this.updatedTime);
        }
    }
}
