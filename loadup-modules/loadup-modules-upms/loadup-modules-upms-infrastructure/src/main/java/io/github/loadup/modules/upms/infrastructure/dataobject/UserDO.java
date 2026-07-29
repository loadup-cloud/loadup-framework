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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User Data Object - Database mapping entity
 */
@Table("upms_user")
public class UserDO extends BaseDO {

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

    private LocalDate birthday;

    private Short status;

    private Boolean accountNonExpired;

    private Boolean accountNonLocked;

    private Boolean credentialsNonExpired;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private Integer loginFailCount;

    private LocalDateTime lockedTime;

    private LocalDateTime passwordUpdateTime;

    private String remark;

    public UserDO(String username, String password, String nickname, String realName, String deptId, String email, Boolean emailVerified, String mobile, Boolean mobileVerified, String avatar, Short gender, LocalDate birthday, Short status, Boolean accountNonExpired, Boolean accountNonLocked, Boolean credentialsNonExpired, LocalDateTime lastLoginTime, String lastLoginIp, Integer loginFailCount, LocalDateTime lockedTime, LocalDateTime passwordUpdateTime, String remark) {
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
        this.remark = remark;
    }

    public UserDO() {
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
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

    public Boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    public Boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    public Boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    public LocalDateTime getLastLoginTime() {
        return this.lastLoginTime;
    }

    public String getLastLoginIp() {
        return this.lastLoginIp;
    }

    public Integer getLoginFailCount() {
        return this.loginFailCount;
    }

    public LocalDateTime getLockedTime() {
        return this.lockedTime;
    }

    public LocalDateTime getPasswordUpdateTime() {
        return this.passwordUpdateTime;
    }

    public String getRemark() {
        return this.remark;
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

    public void setBirthday(LocalDate birthday) {
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

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), username, password, nickname, realName, deptId, email, emailVerified, mobile, mobileVerified, avatar, gender, birthday, status, accountNonExpired, accountNonLocked, credentialsNonExpired, lastLoginTime, lastLoginIp, loginFailCount, lockedTime, passwordUpdateTime, remark);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserDO other = (UserDO) o;
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
        if (!java.util.Objects.equals(remark, other.remark)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UserDO(" + "super=" + super.toString() + ", " + "username=" + username + ", " + "password=" + password + ", " + "nickname=" + nickname + ", " + "realName=" + realName + ", " + "deptId=" + deptId + ", " + "email=" + email + ", " + "emailVerified=" + emailVerified + ", " + "mobile=" + mobile + ", " + "mobileVerified=" + mobileVerified + ", " + "avatar=" + avatar + ", " + "gender=" + gender + ", " + "birthday=" + birthday + ", " + "status=" + status + ", " + "accountNonExpired=" + accountNonExpired + ", " + "accountNonLocked=" + accountNonLocked + ", " + "credentialsNonExpired=" + credentialsNonExpired + ", " + "lastLoginTime=" + lastLoginTime + ", " + "lastLoginIp=" + lastLoginIp + ", " + "loginFailCount=" + loginFailCount + ", " + "lockedTime=" + lockedTime + ", " + "passwordUpdateTime=" + passwordUpdateTime + ", " + "remark=" + remark + ")";
    }
}
