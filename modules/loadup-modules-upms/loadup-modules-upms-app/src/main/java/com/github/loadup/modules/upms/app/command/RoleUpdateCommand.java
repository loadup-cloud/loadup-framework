package com.github.loadup.modules.upms.app.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * Role Update Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class RoleUpdateCommand {

  @NotNull(message = "角色ID不能为空")
  private Long id;

  @Size(max = 50, message = "角色名称长度不能超过50")
  private String roleName;

  private Long parentRoleId;

  /** Data scope: 1-All, 2-Custom, 3-Dept, 4-Dept and children, 5-Self only */
  private Short dataScope;

  private Integer sortOrder;

  private Short status;

  private List<Long> permissionIds;

  private List<Long> departmentIds;

  private String remark;

  private Long updatedBy;
}
