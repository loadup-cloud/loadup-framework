package com.github.loadup.modules.upms.app.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Query Parameters
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuery {

  private String username;
  private String nickname;
  private String realName;
  private String email;
  private String phone;
  private String deptId;
  private Short status;
  private Boolean deleted;

  // Pagination
  private Integer page = 1;
  private Integer size = 20;
  private String sortBy = "createdTime";
  private String sortOrder = "DESC";
}
