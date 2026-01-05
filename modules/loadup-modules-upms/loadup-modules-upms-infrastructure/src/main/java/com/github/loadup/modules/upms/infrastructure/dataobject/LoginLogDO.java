package com.github.loadup.modules.upms.infrastructure.dataobject;

import com.github.loadup.commons.dataobject.BaseDO;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * LoginLog Data Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("upms_login_log")
public class LoginLogDO extends BaseDO {

  private String userId;

  private String username;

  private LocalDateTime loginTime;

  private LocalDateTime logoutTime;

  private String ipAddress;

  private String loginLocation;

  private String browser;

  private String os;

  private Short loginStatus;

  private String loginMessage;
}
