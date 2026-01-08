package com.github.loadup.upms.api.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * User Password Change Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class UserPasswordChangeCommand {

  @NotNull(message = "用户ID不能为空")
  private String userId;

  @NotBlank(message = "旧密码不能为空")
  private String oldPassword;

  @NotBlank(message = "新密码不能为空")
  @Size(min = 6, max = 20, message = "新密码长度必须在6-20之间")
  private String newPassword;

  @NotBlank(message = "确认密码不能为空")
  private String confirmPassword;
}
