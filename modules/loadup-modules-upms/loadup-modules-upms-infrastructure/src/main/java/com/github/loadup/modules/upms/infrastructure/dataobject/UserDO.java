package com.github.loadup.modules.upms.infrastructure.dataobject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

/**
 * User Data Object - Database mapping entity
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("upms_user")
public class UserDO extends BaseDO {

  private String username;

  private String password;

  private String nickname;

  private String realName;

  private String deptId;

  private String email;

  private Boolean emailVerified;

  private String phone;

  private Boolean phoneVerified;

  private String avatarUrl;

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
}
