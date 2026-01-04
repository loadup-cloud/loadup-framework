package com.github.loadup.modules.upms.app.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * User Create Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class UserCreateCommand {

  @NotBlank(message = "用户名不能为空")
  @Size(min = 3, max = 30, message = "用户名长度必须在3-30之间")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
  private String username;

  @NotBlank(message = "密码不能为空")
  @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
  private String password;

  @NotBlank(message = "昵称不能为空")
  @Size(max = 50, message = "昵称长度不能超过50")
  private String nickname;

  @Size(max = 50, message = "真实姓名长度不能超过50")
  private String realName;

  private String deptId;

  @Email(message = "邮箱格式不正确")
  private String email;

  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  private String phone;

  private String avatarUrl;

  private Short gender; // 0-Unknown, 1-Male, 2-Female

  private LocalDate birthday;

  private Short status; // 1-Normal, 0-Disabled

  private List<String> roleIds;

  private String remark;

  private String createdBy;
}
