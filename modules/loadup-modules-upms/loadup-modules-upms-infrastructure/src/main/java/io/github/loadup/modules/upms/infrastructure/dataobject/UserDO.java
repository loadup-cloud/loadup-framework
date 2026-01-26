package io.github.loadup.modules.upms.infrastructure.dataobject;

import io.github.loadup.commons.dataobject.BaseDO;
import com.mybatisflex.annotation.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
}
