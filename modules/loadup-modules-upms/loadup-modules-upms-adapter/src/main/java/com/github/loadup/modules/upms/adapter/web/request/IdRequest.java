package com.github.loadup.modules.upms.adapter.web.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ID Request for single entity operations
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class IdRequest {
  @NotNull(message = "ID不能为空")
  private Long id;
}
