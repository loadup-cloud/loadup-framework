package com.github.loadup.modules.upms.adapter.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Refresh Token Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest {

  @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "刷新令牌不能为空")
  private String refreshToken;
}
