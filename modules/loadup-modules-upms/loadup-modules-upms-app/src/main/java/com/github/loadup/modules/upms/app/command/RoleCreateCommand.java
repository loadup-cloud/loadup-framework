package com.github.loadup.modules.upms.app.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * Role Create Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class RoleCreateCommand {

  @NotBlank(message = "角色名称不能为空")
  @Size(max = 50, message = "角色名称长度不能超过50")
  private String roleName;

  @NotBlank(message = "角色编码不能为空")
  @Size(max = 50, message = "角色编码长度不能超过50")
  @Pattern(regexp = "^[A-Z_]+$", message = "角色编码只能包含大写字母和下划线")
  private String roleCode;

  /** Parent role ID for role inheritance (RBAC3 feature) */
  private String parentRoleId;

  /** Data scope: 1-All, 2-Custom, 3-Dept, 4-Dept and children, 5-Self only */
  private Short dataScope;

  private Integer sortOrder;

  /** Status: 1-Normal, 0-Disabled */
  private Short status;

  private List<String> permissionIds;

  private List<String> departmentIds; // For custom data scope

  private String remark;

  private String createdBy;
}
