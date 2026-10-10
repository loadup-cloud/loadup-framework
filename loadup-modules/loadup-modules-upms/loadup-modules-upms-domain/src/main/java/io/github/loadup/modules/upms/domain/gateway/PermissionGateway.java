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

    /**
     * Save permission
     */
    Permission save(Permission permission);

    /**
     * Update permission
     */
    Permission update(Permission permission);

    /**
     * Delete permission by ID
     */
    void deleteById(String id);

    /**
     * Find permission by ID
     */
    Optional<Permission> findById(String id);

    /**
     * Find permission by code
     */
    Optional<Permission> findByPermissionCode(String permissionCode);

    /**
     * Find permissions by role ID
     */
    List<Permission> findByRoleId(String roleId);

    /**
     * Find permissions by parent ID
     */
    List<Permission> findByParentId(String parentId);

    /**
     * Find permissions by type
     */
    List<Permission> findByPermissionType(Short permissionType);

    /**
     * Find all permissions
     */
    List<Permission> findAll();

    /**
     * Find enabled permissions
     */
    List<Permission> findAllEnabled();

    /**
     * Find menu permissions
     */
    List<Permission> findMenuPermissions();

    /**
     * Check if permission code exists
     */
    boolean existsByPermissionCode(String permissionCode);
}
