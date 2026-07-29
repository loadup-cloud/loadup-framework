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
 * User Entity - Core user domain model
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class User {

    private String id;

    private String username;

    private String password;

    private String nickname;

    private String realName;

    private String deptId;

    private String email;

    private Boolean emailVerified;

    private String mobile;

    private Boolean mobileVerified;

    private String avatar;

    /**
     * Gender: 0-Unknown, 1-Male, 2-Female
     */
    private Short gender;

    private java.time.LocalDate birthday;

    /**
     * Status: 1-Normal, 0-Disabled, 2-Locked
     */
    private Short status;

    private Boolean accountNonExpired;

    private Boolean accountNonLocked;

    private Boolean credentialsNonExpired;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private Integer loginFailCount;

    private LocalDateTime lockedTime;

    private LocalDateTime passwordUpdateTime;

    private Boolean deleted;

    private String remark;

    private String createdBy;

    private LocalDateTime createdTime;

    private String updatedBy;

    private LocalDateTime updatedTime;

    // Transient fields (not persisted)
    private List<Role> roles;

    private Department department;

    /**
     * Check if user account is enabled
     */
    public boolean isEnabled() {
        return status != null && status == 1 && !Boolean.TRUE.equals(deleted);
    }

    /**
     * Check if user account is active (enabled and not locked)
     */
    public boolean isActive() {
        return isEnabled()
                && Boolean.TRUE.equals(accountNonExpired)
                && Boolean.TRUE.equals(accountNonLocked)
                && Boolean.TRUE.equals(credentialsNonExpired);
    }

    /**
     * Increment login fail count
     */
    public void incrementLoginFailCount() {
        if (this.loginFailCount == null) {
            this.loginFailCount = 0;
        }
        this.loginFailCount++;
    }

    /**
     * Reset login fail count
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.lockedTime = null;
    }

    /**
     * Lock user account
     */
    public void lockAccount() {
        this.accountNonLocked = false;
        this.lockedTime = LocalDateTime.now();
        this.status = 2; // Locked status
    }

    /**
     * Unlock user account
     */
    public void unlockAccount() {
        this.accountNonLocked = true;
        this.lockedTime = null;
        this.loginFailCount = 0;
        this.status = 1; // Normal status
    }

    /**
     * Update last login info
     */
    public void updateLastLogin(String ip) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = ip;
        this.resetLoginFailCount();
    }

    public User(String id, String username, String password, String nickname, String realName, String deptId, String email, Boolean emailVerified, String mobile, Boolean mobileVerified, String avatar, Short gender, java.time.LocalDate birthday, Short status, Boolean accountNonExpired, Boolean accountNonLocked, Boolean credentialsNonExpired, LocalDateTime lastLoginTime, String lastLoginIp, Integer loginFailCount, LocalDateTime lockedTime, LocalDateTime passwordUpdateTime, Boolean deleted, String remark, String createdBy, LocalDateTime createdTime, String updatedBy, LocalDateTime updatedTime, List<Role> roles, Department department) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.realName = realName;
        this.deptId = deptId;
        this.email = email;
        this.emailVerified = emailVerified;
        this.mobile = mobile;
        this.mobileVerified = mobileVerified;
        this.avatar = avatar;
        this.gender = gender;
        this.birthday = birthday;
        this.status = status;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.lastLoginTime = lastLoginTime;
        this.lastLoginIp = lastLoginIp;
        this.loginFailCount = loginFailCount;
        this.lockedTime = lockedTime;
        this.passwordUpdateTime = passwordUpdateTime;
        this.deleted = deleted;
        this.remark = remark;
        this.createdBy = createdBy;
        this.createdTime = createdTime;
        this.updatedBy = updatedBy;
        this.updatedTime = updatedTime;
        this.roles = roles;
        this.department = department;
    }

    public User() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public void setBirthday(java.time.LocalDate birthday) {
        this.birthday = birthday;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public void setLoginFailCount(Integer loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public void setLockedTime(LocalDateTime lockedTime) {
        this.lockedTime = lockedTime;
    }

    public void setPasswordUpdateTime(LocalDateTime passwordUpdateTime) {
        this.passwordUpdateTime = passwordUpdateTime;
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

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, username, password, nickname, realName, deptId, email, emailVerified, mobile, mobileVerified, avatar, gender, birthday, status, accountNonExpired, accountNonLocked, credentialsNonExpired, lastLoginTime, lastLoginIp, loginFailCount, lockedTime, passwordUpdateTime, deleted, remark, createdBy, createdTime, updatedBy, updatedTime, roles, department);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User other = (User) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(username, other.username)) return false;
        if (!java.util.Objects.equals(password, other.password)) return false;
        if (!java.util.Objects.equals(nickname, other.nickname)) return false;
        if (!java.util.Objects.equals(realName, other.realName)) return false;
        if (!java.util.Objects.equals(deptId, other.deptId)) return false;
        if (!java.util.Objects.equals(email, other.email)) return false;
        if (!java.util.Objects.equals(emailVerified, other.emailVerified)) return false;
        if (!java.util.Objects.equals(mobile, other.mobile)) return false;
        if (!java.util.Objects.equals(mobileVerified, other.mobileVerified)) return false;
        if (!java.util.Objects.equals(avatar, other.avatar)) return false;
        if (!java.util.Objects.equals(gender, other.gender)) return false;
        if (!java.util.Objects.equals(birthday, other.birthday)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(accountNonExpired, other.accountNonExpired)) return false;
        if (!java.util.Objects.equals(accountNonLocked, other.accountNonLocked)) return false;
        if (!java.util.Objects.equals(credentialsNonExpired, other.credentialsNonExpired)) return false;
        if (!java.util.Objects.equals(lastLoginTime, other.lastLoginTime)) return false;
        if (!java.util.Objects.equals(lastLoginIp, other.lastLoginIp)) return false;
        if (!java.util.Objects.equals(loginFailCount, other.loginFailCount)) return false;
        if (!java.util.Objects.equals(lockedTime, other.lockedTime)) return false;
        if (!java.util.Objects.equals(passwordUpdateTime, other.passwordUpdateTime)) return false;
        if (!java.util.Objects.equals(deleted, other.deleted)) return false;
        if (!java.util.Objects.equals(remark, other.remark)) return false;
        if (!java.util.Objects.equals(createdBy, other.createdBy)) return false;
        if (!java.util.Objects.equals(createdTime, other.createdTime)) return false;
        if (!java.util.Objects.equals(updatedBy, other.updatedBy)) return false;
        if (!java.util.Objects.equals(updatedTime, other.updatedTime)) return false;
        if (!java.util.Objects.equals(roles, other.roles)) return false;
        if (!java.util.Objects.equals(department, other.department)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "User(" + "id=" + id + ", " + "username=" + username + ", " + "password=" + password + ", " + "nickname=" + nickname + ", " + "realName=" + realName + ", " + "deptId=" + deptId + ", " + "email=" + email + ", " + "emailVerified=" + emailVerified + ", " + "mobile=" + mobile + ", " + "mobileVerified=" + mobileVerified + ", " + "avatar=" + avatar + ", " + "gender=" + gender + ", " + "birthday=" + birthday + ", " + "status=" + status + ", " + "accountNonExpired=" + accountNonExpired + ", " + "accountNonLocked=" + accountNonLocked + ", " + "credentialsNonExpired=" + credentialsNonExpired + ", " + "lastLoginTime=" + lastLoginTime + ", " + "lastLoginIp=" + lastLoginIp + ", " + "loginFailCount=" + loginFailCount + ", " + "lockedTime=" + lockedTime + ", " + "passwordUpdateTime=" + passwordUpdateTime + ", " + "deleted=" + deleted + ", " + "remark=" + remark + ", " + "createdBy=" + createdBy + ", " + "createdTime=" + createdTime + ", " + "updatedBy=" + updatedBy + ", " + "updatedTime=" + updatedTime + ", " + "roles=" + roles + ", " + "department=" + department + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String username;
        private String password;
        private String nickname;
        private String realName;
        private String deptId;
        private String email;
        private Boolean emailVerified;
        private String mobile;
        private Boolean mobileVerified;
        private String avatar;
        private Short gender;
        private java.time.LocalDate birthday;
        private Short status;
        private Boolean accountNonExpired;
        private Boolean accountNonLocked;
        private Boolean credentialsNonExpired;
        private LocalDateTime lastLoginTime;
        private String lastLoginIp;
        private Integer loginFailCount;
        private LocalDateTime lockedTime;
        private LocalDateTime passwordUpdateTime;
        private Boolean deleted;
        private String remark;
        private String createdBy;
        private LocalDateTime createdTime;
        private String updatedBy;
        private LocalDateTime updatedTime;
        private List<Role> roles;
        private Department department;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
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

        public Builder birthday(java.time.LocalDate birthday) {
            this.birthday = birthday;
            return this;
        }

        public Builder status(Short status) {
            this.status = status;
            return this;
        }

        public Builder accountNonExpired(Boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }

        public Builder accountNonLocked(Boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }

        public Builder credentialsNonExpired(Boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
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

        public Builder loginFailCount(Integer loginFailCount) {
            this.loginFailCount = loginFailCount;
            return this;
        }

        public Builder lockedTime(LocalDateTime lockedTime) {
            this.lockedTime = lockedTime;
            return this;
        }

        public Builder passwordUpdateTime(LocalDateTime passwordUpdateTime) {
            this.passwordUpdateTime = passwordUpdateTime;
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

        public Builder roles(List<Role> roles) {
            this.roles = roles;
            return this;
        }

        public Builder department(Department department) {
            this.department = department;
            return this;
        }

        public User build() {
            return new User(this.id, this.username, this.password, this.nickname, this.realName, this.deptId, this.email, this.emailVerified, this.mobile, this.mobileVerified, this.avatar, this.gender, this.birthday, this.status, this.accountNonExpired, this.accountNonLocked, this.credentialsNonExpired, this.lastLoginTime, this.lastLoginIp, this.loginFailCount, this.lockedTime, this.passwordUpdateTime, this.deleted, this.remark, this.createdBy, this.createdTime, this.updatedBy, this.updatedTime, this.roles, this.department);
        }
    }
}
