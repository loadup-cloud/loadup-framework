package com.github.loadup.upms.api.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Department Update Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class DepartmentUpdateCommand {

  @NotNull(message = "部门ID不能为空")
  private String id;

  private String parentId;

  @Size(max = 50, message = "部门名称长度不能超过50")
  private String deptName;

  private Integer sortOrder;

  private String leaderUserId;

  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  private String mobile;

  @Email(message = "邮箱格式不正确")
  private String email;

  private Short status;

  private String remark;

  private String updatedBy;
}
