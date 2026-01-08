package com.github.loadup.upms.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role DTO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

  private String id;
  private String roleName;
  private String roleCode;
  private String parentId;
  private String parentRoleName;
  private Integer roleLevel;
  private Short dataScope;
  private Integer sortOrder;
  private Short status;
  private List<PermissionDTO> permissions;
  private List<String> departmentIds;
  private String remark;
  private LocalDateTime createdTime;
  private LocalDateTime updatedTime;
}
