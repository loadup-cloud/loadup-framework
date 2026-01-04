package com.github.loadup.modules.upms.adapter.web.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Assign Role Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class AssignRoleRequest {
  @NotNull(message = "角色ID不能为空")
  private String roleId;

  @NotNull(message = "用户ID不能为空")
  private String userId;
}
