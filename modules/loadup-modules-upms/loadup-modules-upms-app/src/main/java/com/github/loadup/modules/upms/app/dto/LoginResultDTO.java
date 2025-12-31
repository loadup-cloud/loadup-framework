package com.github.loadup.modules.upms.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Result DTO
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

  private String tokenType = "Bearer";

  private Long expiresIn;

  private UserInfoDTO userInfo;
}
