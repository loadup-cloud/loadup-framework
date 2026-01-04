package com.github.loadup.modules.upms.domain.repository;

import com.github.loadup.modules.upms.domain.entity.Role;
import java.util.List;
import java.util.Optional;

/**
 * Role Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface RoleRepository {

  /** Save role */
  Role save(Role role);

  /** Update role */
  Role update(Role role);

  /** Delete role by ID */
  void deleteById(Long id);

  /** Find role by ID */
  Optional<Role> findById(Long id);

  /** Find role by code */
  Optional<Role> findByRoleCode(String roleCode);

  /** Find roles by user ID */
  List<Role> findByUserId(Long userId);

  /** Find roles by parent role ID */
  List<Role> findByParentRoleId(Long parentRoleId);

  /** Find all roles */
  List<Role> findAll();

  /** Find enabled roles */
  List<Role> findAllEnabled();

  /** Check if role code exists */
  boolean existsByRoleCode(String roleCode);

  /** Assign role to user */
  void assignRoleToUser(Long userId, Long roleId, Long operatorId);

  /** Remove role from user */
  void removeRoleFromUser(Long userId, Long roleId);

  /** Get user's role IDs */
  List<Long> getUserRoleIds(Long userId);

  /** Assign permissions to role (batch) */
  void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

  /** Remove permissions from role (batch) */
  void removePermissionsFromRole(Long roleId, List<Long> permissionIds);

  /** Assign departments to role (for custom data scope) */
  void assignDepartmentsToRole(Long roleId, List<Long> departmentIds);

  /** Remove departments from role */
  void removeDepartmentsFromRole(Long roleId, List<Long> departmentIds);

  /** Get department IDs by role ID (for custom data scope) */
  List<Long> findDepartmentIdsByRoleId(Long roleId);

  /** Count users by role ID */
  long countUsersByRoleId(Long roleId);
}
