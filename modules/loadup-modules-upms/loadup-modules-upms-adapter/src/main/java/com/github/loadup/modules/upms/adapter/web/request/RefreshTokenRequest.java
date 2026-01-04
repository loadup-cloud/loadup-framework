package com.github.loadup.modules.upms.adapter.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Refresh Token Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class RefreshTokenRequest {
  @NotBlank(message = "刷新令牌不能为空")
  private String refreshToken;
}
