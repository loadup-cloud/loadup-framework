package com.github.loadup.modules.upms.app.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * User Password Reset Command (with verification code)
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class UserPasswordResetCommand {

  @NotBlank(message = "用户名不能为空")
  private String username;

  @NotBlank(message = "验证码不能为空")
  private String verificationCode;

  @NotBlank(message = "新密码不能为空")
  @Size(min = 6, max = 20, message = "新密码长度必须在6-20之间")
  private String newPassword;

  @NotBlank(message = "确认密码不能为空")
  private String confirmPassword;

  /** Verification type: EMAIL or SMS */
  private String verificationType;
}
