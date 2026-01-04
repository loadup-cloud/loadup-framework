package com.github.loadup.modules.upms.domain.repository;

import com.github.loadup.modules.upms.domain.entity.Permission;
import java.util.List;
import java.util.Optional;

/**
 * Permission Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface PermissionRepository {

  /** Save permission */
  Permission save(Permission permission);

  /** Update permission */
  Permission update(Permission permission);

  /** Delete permission by ID */
  void deleteById(Long id);

  /** Find permission by ID */
  Optional<Permission> findById(Long id);

  /** Find permission by code */
  Optional<Permission> findByPermissionCode(String permissionCode);

  /** Find permissions by user ID */
  List<Permission> findByUserId(Long userId);

  /** Find permissions by role ID */
  List<Permission> findByRoleId(Long roleId);

  /** Find permissions by parent ID */
  List<Permission> findByParentId(Long parentId);

  /** Find permissions by type */
  List<Permission> findByPermissionType(Short permissionType);

  /** Find all permissions */
  List<Permission> findAll();

  /** Find enabled permissions */
  List<Permission> findAllEnabled();

  /** Find menu permissions */
  List<Permission> findMenuPermissions();

  /** Check if permission code exists */
  boolean existsByPermissionCode(String permissionCode);

  /** Assign permission to role */
  void assignPermissionToRole(Long roleId, Long permissionId, Long operatorId);

  /** Remove permission from role */
  void removePermissionFromRole(Long roleId, Long permissionId);

  /** Batch assign permissions to role */
  void batchAssignPermissionsToRole(Long roleId, List<Long> permissionIds, Long operatorId);

  /** Remove all permissions from role */
  void removeAllPermissionsFromRole(Long roleId);

  /** Get role's permission IDs */
  List<Long> getRolePermissionIds(Long roleId);
}
