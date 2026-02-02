package io.github.loadup.modules.upms.domain.gateway;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.modules.upms.domain.entity.Role;
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
  List<Role> findByParentId(String parentId);

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
