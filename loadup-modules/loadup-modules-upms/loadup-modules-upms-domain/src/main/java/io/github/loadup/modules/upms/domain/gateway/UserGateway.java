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
import io.github.loadup.modules.upms.domain.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * User Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface UserGateway {

    /**
     * Save user
     */
    User save(User user);

    /**
     * Update user
     */
    User update(User user);

    /**
     * Delete user by ID
     */
    void deleteById(String id);

    /**
     * Find user by ID
     */
    Optional<User> findById(String id);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone
     */
    Optional<User> findByMobile(String mobile);

    /**
     * Find users by department ID
     */
    List<User> findByDeptId(String deptId);

    /**
     * Find users by role ID
     */
    List<User> findByRoleId(String roleId);

    /**
     * Find all users (with pagination)
     */
    PageDTO<User> findAll(PageQuery query);

    /**
     * Search users by keyword
     */
    PageDTO<User> search(String keyword, PageQuery query);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if mobile exists
     */
    boolean existsByMobile(String mobile);

    /**
     * Count users by department ID
     */
    long countByDeptId(String deptId);
}
