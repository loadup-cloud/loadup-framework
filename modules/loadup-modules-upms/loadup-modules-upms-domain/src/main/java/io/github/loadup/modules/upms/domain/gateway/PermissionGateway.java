package io.github.loadup.modules.upms.domain.gateway;

import io.github.loadup.modules.upms.domain.entity.Permission;
import java.util.List;
import java.util.Optional;

/**
 * Permission Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface PermissionGateway {

  /** Save permission */
  Permission save(Permission permission);

  /** Update permission */
  Permission update(Permission permission);

  /** Delete permission by ID */
  void deleteById(String id);

  /** Find permission by ID */
  Optional<Permission> findById(String id);

  /** Find permission by code */
  Optional<Permission> findByPermissionCode(String permissionCode);

  /** Find permissions by user ID */
  List<Permission> findByUserId(String userId);

  /** Find permissions by role ID */
  List<Permission> findByRoleId(String roleId);

  /** Find permissions by parent ID */
  List<Permission> findByParentId(String parentId);

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
  void assignPermissionToRole(String roleId, String permissionId, String operatorId);

  /** Remove permission from role */
  void removePermissionFromRole(String roleId, String permissionId);

  /** Batch assign permissions to role */
  void batchAssignPermissionsToRole(String roleId, List<String> permissionIds, String operatorId);

  /** Remove all permissions from role */
  void removeAllPermissionsFromRole(String roleId);

  /** Get role's permission IDs */
  List<String> getRolePermissionIds(String roleId);
}
