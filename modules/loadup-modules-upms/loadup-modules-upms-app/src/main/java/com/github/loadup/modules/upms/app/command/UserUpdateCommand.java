package com.github.loadup.modules.upms.app.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data

/**
 * @since 1.0.0
 * @author LoadUp Framework
 *     <p>User Update Command
 */
public class UserUpdateCommand {

  private String updatedBy;

  private String remark;

  private List<String> roleIds;

  private Short status; // 1-Normal, 0-Disabled

  private LocalDate birthday;

  private Short gender; // 0-Unknown, 1-Male, 2-Female

  private String avatarUrl;

  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  private String phone;

  @Email(message = "邮箱格式不正确")
  private String email;

  private String deptId;

  @Size(max = 50, message = "真实姓名长度不能超过50")
  private String realName;

  @Size(max = 50, message = "昵称长度不能超过50")
  private String nickname;

  @NotNull(message = "用户ID不能为空")
  private String id;
}
