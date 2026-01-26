package io.github.loadup.modules.upms.domain.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Log Entity - User login/logout audit log
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLog {

  private String id;

  private String userId;

  private String username;

  private LocalDateTime loginTime;

  private LocalDateTime logoutTime;

  private String ipAddress;

  private String loginLocation;

  private String browser;

  private String os;

  /** Login status: 1-Success, 0-Failure */
  private Short loginStatus;

  private String loginMessage;

  public boolean isSuccess() {
    return loginStatus != null && loginStatus == 1;
  }
}
