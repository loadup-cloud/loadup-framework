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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Entity - Core user domain model
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    /** Gender: 0-Unknown, 1-Male, 2-Female */
    private Short gender;

    private java.time.LocalDate birthday;

    /** Status: 1-Normal, 0-Disabled, 2-Locked */
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

    /** Check if user account is enabled */
    public boolean isEnabled() {
        return status != null && status == 1 && !Boolean.TRUE.equals(deleted);
    }

    /** Check if user account is active (enabled and not locked) */
    public boolean isActive() {
        return isEnabled()
                && Boolean.TRUE.equals(accountNonExpired)
                && Boolean.TRUE.equals(accountNonLocked)
                && Boolean.TRUE.equals(credentialsNonExpired);
    }

    /** Increment login fail count */
    public void incrementLoginFailCount() {
        if (this.loginFailCount == null) {
            this.loginFailCount = 0;
        }
        this.loginFailCount++;
    }

    /** Reset login fail count */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.lockedTime = null;
    }

    /** Lock user account */
    public void lockAccount() {
        this.accountNonLocked = false;
        this.lockedTime = LocalDateTime.now();
        this.status = 2; // Locked status
    }

    /** Unlock user account */
    public void unlockAccount() {
        this.accountNonLocked = true;
        this.lockedTime = null;
        this.loginFailCount = 0;
        this.status = 1; // Normal status
    }

    /** Update last login info */
    public void updateLastLogin(String ip) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = ip;
        this.resetLoginFailCount();
    }
}
