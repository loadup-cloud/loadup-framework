package io.github.loadup.modules.upms.adapter.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Register Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class RegisterRequest {
  @NotBlank(message = "用户名不能为空")
  private String username;

  @NotBlank(message = "密码不能为空")
  private String password;

  private String nickname;

  @Email(message = "邮箱格式不正确")
  private String email;

  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  private String mobile;

  private String captchaKey;
  private String captchaCode;
  private String smsCode;
}
