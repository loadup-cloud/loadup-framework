package io.github.loadup.modules.upms.infrastructure.dataobject;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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

    public UserDO(
            String username,
            String password,
            String nickname,
            String realName,
            String deptId,
            String email,
            Boolean emailVerified,
            String mobile,
            Boolean mobileVerified,
            String avatar,
            Short gender,
            LocalDate birthday,
            Short status,
            Boolean accountNonExpired,
            Boolean accountNonLocked,
            Boolean credentialsNonExpired,
            LocalDateTime lastLoginTime,
            String lastLoginIp,
            Integer loginFailCount,
            LocalDateTime lockedTime,
            LocalDateTime passwordUpdateTime,
            String remark) {
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

    public UserDO() {}

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
}
