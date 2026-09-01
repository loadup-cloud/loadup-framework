package io.github.loadup.modules.upms.domain.gateway;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.github.loadup.commons.dto.PageQuery;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.domain.entity.Role;
import java.util.List;
import java.util.Optional;

/**
 * Role Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface RoleGateway {

    /**
     * Save role
     */
    Role save(Role role);

    /**
     * Update role
     */
    Role update(Role role);

    /**
     * Delete role by ID
     */
    void deleteById(String id);

    /**
     * Find role by ID
     */
    Optional<Role> findById(String id);

    /**
     * Find role by code
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * Find roles by user ID
     */
    List<Role> findByUserId(String userId);

    /**
     * Find roles by parent role ID
     */
    List<Role> findByParentId(String parentId);

    /**
     * Find all roles
     */
    List<Role> findAll();

    /**
     * Find enabled roles
     */
    List<Role> findAllEnabled();

    /**
     * Check if role code exists
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * Assign role to user
     */
    void assignRoleToUser(String userId, String roleId, String operatorId);

    /**
     * Remove role from user
     */
    void removeRoleFromUser(String userId, String roleId);

    /**
     * Get user's role IDs
     */
    List<String> getUserRoleIds(String userId);

    /**
     * Assign permissions to role (batch)
     */
    void assignPermissionsToRole(String roleId, List<String> permissionIds);

    /**
     * Remove permissions from role (batch)
     */
    void removePermissionsFromRole(String roleId, List<String> permissionIds);

    /**
     * Assign departments to role (for custom data scope)
     */
    void assignDepartmentsToRole(String roleId, List<String> departmentIds);

    /**
     * Remove departments from role
     */
    void removeDepartmentsFromRole(String roleId, List<String> departmentIds);

    /**
     * Get department IDs by role ID (for custom data scope)
     */
    List<String> findDepartmentIdsByRoleId(String roleId);

    PageDTO<Role> findAll(PageQuery query);

    /**
     * Count users by role ID
     */
    long countUsersByRoleId(String roleId);
}
