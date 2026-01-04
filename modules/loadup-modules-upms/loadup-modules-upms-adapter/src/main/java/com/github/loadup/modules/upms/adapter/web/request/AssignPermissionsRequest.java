package com.github.loadup.modules.upms.adapter.web.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * Assign Permissions Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class AssignPermissionsRequest {
  @NotNull(message = "角色ID不能为空")
  private Long roleId;

  @NotEmpty(message = "权限ID列表不能为空")
  private List<Long> permissionIds;
}
