package com.github.loadup.modules.upms.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

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
@Table("upms_role")
public class Role {

  @Id private Long id;

  private String roleName;

  private String roleCode;

  /** Parent role ID for role inheritance (RBAC3 feature) */
  private Long parentRoleId;

  private Integer roleLevel;

  /** Data scope: 1-All, 2-Custom, 3-Dept, 4-Dept and children, 5-Self only */
  private Short dataScope;

  private Integer sortOrder;

  /** Status: 1-Normal, 0-Disabled */
  private Short status;

  private Boolean deleted;

  private String remark;

  private Long createdBy;

  private LocalDateTime createdTime;

  private Long updatedBy;

  private LocalDateTime updatedTime;

  // Transient fields
  @Transient private Role parentRole;

  @Transient private List<Role> childRoles;

  @Transient private List<Permission> permissions;

  @Transient private List<Department> departments;

  /** Check if role is enabled */
  public boolean isEnabled() {
    return status != null && status == 1 && !Boolean.TRUE.equals(deleted);
  }

  /** Check if this is a root role (no parent) */
  public boolean isRoot() {
    return parentRoleId == null;
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
