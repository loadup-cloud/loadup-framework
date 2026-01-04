package com.github.loadup.modules.upms.infrastructure.dataobject;

import com.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

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

  @Id private String id;
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
