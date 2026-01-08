package com.github.loadup.modules.upms.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role Entity - RBAC3 Role with hierarchy support
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

  private String id;

  private String roleName;

  private String roleCode;

  /** Parent role ID for role inheritance (RBAC3 feature) */
  private String parentId;

  private Integer roleLevel;

  /** Data scope: 1-All, 2-Custom, 3-Dept, 4-Dept and children, 5-Self only */
  private Short dataScope;

  private Integer sortOrder;

  /** Status: 1-Normal, 0-Disabled */
  private Short status;

  private Boolean deleted;

  private String remark;

  private String createdBy;

  private LocalDateTime createdTime;

  private String updatedBy;

  private LocalDateTime updatedTime;

  // Transient fields
  private Role parentRole;

  private List<Role> childRoles;

  private List<Permission> permissions;

  private List<Department> departments;

  /** Check if role is enabled */
  public boolean isEnabled() {
    return status != null && status == 1 && !Boolean.TRUE.equals(deleted);
  }

  /** Check if this is a root role (no parent) */
  public boolean isRoot() {
    return parentId == null;
  }

  /** Get all inherited permissions (including parent roles) */
  public List<Permission> getAllInheritedPermissions() {
    List<Permission> allPermissions = new ArrayList<>();
    if (permissions != null) {
      allPermissions.addAll(permissions);
    }

    // Recursively add parent role permissions
    if (parentRole != null && parentRole.isEnabled()) {
      allPermissions.addAll(parentRole.getAllInheritedPermissions());
    }

    return allPermissions;
  }
}
