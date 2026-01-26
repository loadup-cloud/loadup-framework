package io.github.loadup.upms.api.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Department Create Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class DepartmentCreateCommand {

  private String parentId;

  @NotBlank(message = "部门名称不能为空")
  @Size(max = 50, message = "部门名称长度不能超过50")
  private String deptName;

  @NotBlank(message = "部门编码不能为空")
  @Size(max = 50, message = "部门编码长度不能超过50")
  @Pattern(regexp = "^[A-Z0-9_]+$", message = "部门编码只能包含大写字母、数字和下划线")
  private String deptCode;

  private Integer sortOrder;

  private String leaderUserId;

  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  private String mobile;

  @Email(message = "邮箱格式不正确")
  private String email;

  /** Status: 1-Normal, 0-Disabled */
  private Short status;

  private String remark;

  private String createdBy;
}
