package com.github.loadup.modules.upms.domain.gateway;

import com.github.loadup.modules.upms.domain.entity.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Role Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface RoleGateway {

  /** Save role */
  Role save(Role role);

  /** Update role */
  Role update(Role role);

  /** Delete role by ID */
  void deleteById(String id);

  /** Find role by ID */
  Optional<Role> findById(String id);

  /** Find role by code */
  Optional<Role> findByRoleCode(String roleCode);

  /** Find roles by user ID */
  List<Role> findByUserId(String userId);

  /** Find roles by parent role ID */
  List<Role> findByParentRoleId(String parentRoleId);

  /** Find all roles */
  List<Role> findAll();

  /** Find enabled roles */
  List<Role> findAllEnabled();

  /** Check if role code exists */
  boolean existsByRoleCode(String roleCode);

  /** Assign role to user */
  void assignRoleToUser(String userId, String roleId, String operatorId);

  /** Remove role from user */
  void removeRoleFromUser(String userId, String roleId);

  /** Get user's role IDs */
  List<String> getUserRoleIds(String userId);

  /** Assign permissions to role (batch) */
  void assignPermissionsToRole(String roleId, List<String> permissionIds);

  /** Remove permissions from role (batch) */
  void removePermissionsFromRole(String roleId, List<String> permissionIds);

  /** Assign departments to role (for custom data scope) */
  void assignDepartmentsToRole(String roleId, List<String> departmentIds);

  /** Remove departments from role */
  void removeDepartmentsFromRole(String roleId, List<String> departmentIds);

  /** Get department IDs by role ID (for custom data scope) */
  List<String> findDepartmentIdsByRoleId(String roleId);

  Page<Role> findAll(Pageable pageable);

  /** Count users by role ID */
  long countUsersByRoleId(String roleId);
}
