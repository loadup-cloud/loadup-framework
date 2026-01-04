package com.github.loadup.modules.upms.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Result DTO - Contains access token and user information
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultDTO {

  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private Long expiresIn;
  private UserInfoDTO userInfo;
}
