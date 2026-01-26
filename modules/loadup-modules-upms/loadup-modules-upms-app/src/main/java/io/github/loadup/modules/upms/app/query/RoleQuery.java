package io.github.loadup.modules.upms.app.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role Query Parameters
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleQuery {

  private String roleName;
  private String roleCode;
  private String parentId;
  private Short status;
  private Boolean deleted;

  // Pagination
  private Integer page = 1;
  private Integer size = 20;
  private String sortBy = "sortOrder";
  private String sortOrder = "ASC";
}
