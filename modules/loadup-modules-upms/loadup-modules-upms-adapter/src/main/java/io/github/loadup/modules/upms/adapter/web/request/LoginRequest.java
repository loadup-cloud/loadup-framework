package io.github.loadup.modules.upms.adapter.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class LoginRequest {
  @NotBlank(message = "用户名不能为空")
  private String username;

  @NotBlank(message = "密码不能为空")
  private String password;

  private String captchaKey;
  private String captchaCode;
}
