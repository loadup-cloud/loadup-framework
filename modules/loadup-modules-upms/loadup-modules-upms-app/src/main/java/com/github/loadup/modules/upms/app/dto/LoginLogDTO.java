package com.github.loadup.modules.upms.app.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Log DTO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogDTO {

  private Long id;
  private Long userId;
  private String username;
  private String loginType;
  private String ipAddress;
  private String location;
  private String browser;
  private String os;
  private Boolean success;
  private String message;
  private LocalDateTime loginTime;
}
