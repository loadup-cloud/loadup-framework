package com.github.loadup.modules.upms.adapter.web.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Permission Type Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class PermissionTypeRequest {
  @NotNull(message = "权限类型不能为空")
  private Short permissionType;
}
