package com.github.loadup.modules.upms.domain.service;

import com.github.loadup.modules.upms.domain.entity.Permission;
import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import com.github.loadup.modules.upms.domain.gateway.RoleGateway;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * User Permission Domain Service Handles complex permission calculation logic including role
 * inheritance
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserPermissionService {

  private final RoleGateway roleGateway;
  private final PermissionGateway permissionGateway;

  /**
   * Get all permissions for a user (including inherited from parent roles) Implements RBAC3 role
   * hierarchy
   */
  public List<Permission> getUserPermissions(String userId) {
    // Get user's direct roles
    List<Role> userRoles = roleGateway.findByUserId(userId);

    // Collect all permissions including inherited
    Set<Permission> allPermissions = new HashSet<>();

    for (Role role : userRoles) {
      if (role.isEnabled()) {
        // Add role's direct permissions
        List<Permission> rolePermissions = permissionGateway.findByRoleId(role.getId());
        allPermissions.addAll(
            rolePermissions.stream().filter(Permission::isEnabled).collect(Collectors.toList()));

        // Add inherited permissions from parent roles
        allPermissions.addAll(getInheritedPermissions(role));
      }
    }

    return new ArrayList<>(allPermissions);
  }

  /** Get inherited permissions from parent roles recursively */
  private Set<Permission> getInheritedPermissions(Role role) {
    Set<Permission> inherited = new HashSet<>();

    if (role.getParentId() != null) {
      roleGateway
          .findById(role.getParentId())
          .ifPresent(
              parentRole -> {
                if (parentRole.isEnabled()) {
                  // Add parent's permissions
                  List<Permission> parentPermissions =
                      permissionGateway.findByRoleId(parentRole.getId());
                  inherited.addAll(
                      parentPermissions.stream()
                          .filter(Permission::isEnabled)
                          .collect(Collectors.toList()));

                  // Recursively add grandparent permissions
                  inherited.addAll(getInheritedPermissions(parentRole));
                }
              });
    }

    return inherited;
  }

  /** Get permission codes for a user */
  public Set<String> getUserPermissionCodes(String userId) {
    return getUserPermissions(userId).stream()
        .map(Permission::getPermissionCode)
        .collect(Collectors.toSet());
  }

  /** Check if user has specific permission */
  public boolean hasPermission(String userId, String permissionCode) {
    return getUserPermissionCodes(userId).contains(permissionCode);
  }

  /** Check if user has any of the specified permissions */
  public boolean hasAnyPermission(String userId, String... permissionCodes) {
    Set<String> userPermissions = getUserPermissionCodes(userId);
    for (String code : permissionCodes) {
      if (userPermissions.contains(code)) {
        return true;
      }
    }
    return false;
  }

  /** Check if user has all of the specified permissions */
  public boolean hasAllPermissions(String userId, String... permissionCodes) {
    Set<String> userPermissions = getUserPermissionCodes(userId);
    for (String code : permissionCodes) {
      if (!userPermissions.contains(code)) {
        return false;
      }
    }
    return true;
  }

  /** Get menu permissions for a user */
  public List<Permission> getUserMenuPermissions(String userId) {
    return getUserPermissions(userId).stream()
        .filter(Permission::isMenu)
        .filter(p -> Boolean.TRUE.equals(p.getVisible()))
        .sorted(
            (p1, p2) -> {
              int order1 = p1.getSortOrder() != null ? p1.getSortOrder() : 0;
              int order2 = p2.getSortOrder() != null ? p2.getSortOrder() : 0;
              return Integer.compare(order1, order2);
            })
        .collect(Collectors.toList());
  }

  /** Build permission tree for user */
  public List<Permission> buildUserPermissionTree(String userId) {
    List<Permission> allPermissions = getUserPermissions(userId);
    return buildTree(allPermissions, "0");
  }

  /** Recursively build permission tree */
  private List<Permission> buildTree(List<Permission> permissions, String parentId) {
    return permissions.stream()
        .filter(
            p -> {
              if ("0".equals(parentId)) {
                return p.getParentId() == null || "0".equals(p.getParentId());
              }
              return parentId.equals(p.getParentId());
            })
        .peek(
            p -> {
              List<Permission> children = buildTree(permissions, p.getId());
              if (!children.isEmpty()) {
                p.setChildren(children);
              }
            })
        .collect(Collectors.toList());
  }
}
