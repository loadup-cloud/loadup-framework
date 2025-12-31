package com.github.loadup.modules.upms.domain.entity;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

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
@Table("upms_user")
public class User {

  @Id private Long id;

  private String username;

  private String password;

  private String nickname;

  private String realName;

  private Long deptId;

  private String email;

  private Boolean emailVerified;

  private String phone;

  private Boolean phoneVerified;

  private String avatarUrl;

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

  private Long createdBy;

  private LocalDateTime createdTime;

  private Long updatedBy;

  private LocalDateTime updatedTime;

  // Transient fields (not persisted)
  @Transient private List<Role> roles;

  @Transient private Department department;

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
