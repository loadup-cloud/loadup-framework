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
