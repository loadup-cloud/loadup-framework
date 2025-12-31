package com.github.loadup.modules.upms.adapter.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

  @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "用户名不能为空")
  private String username;

  @Schema(description = "密码", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "密码不能为空")
  private String password;

  @Schema(description = "验证码Key")
  private String captchaKey;

  @Schema(description = "验证码")
  private String captchaCode;
}
