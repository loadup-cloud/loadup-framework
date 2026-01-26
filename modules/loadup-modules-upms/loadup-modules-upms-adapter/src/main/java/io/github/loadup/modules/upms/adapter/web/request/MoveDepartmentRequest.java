package io.github.loadup.modules.upms.adapter.web.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Move Department Request
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class MoveDepartmentRequest {
  @NotNull(message = "部门ID不能为空")
  private String deptId;

  private String newParentId;
}
