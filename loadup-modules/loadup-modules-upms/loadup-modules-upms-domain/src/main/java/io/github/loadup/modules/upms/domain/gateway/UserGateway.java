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

import io.github.loadup.modules.upms.domain.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * User Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface UserGateway {

  /** Save user */
  User save(User user);

  /** Update user */
  User update(User user);

  /** Delete user by ID */
  void deleteById(String id);

  /** Find user by ID */
  Optional<User> findById(String id);

  /** Find user by username */
  Optional<User> findByUsername(String username);

  /** Find user by email */
  Optional<User> findByEmail(String email);

  /** Find user by phone */
  Optional<User> findByMobile(String mobile);

  /** Find users by department ID */
  List<User> findByDeptId(String deptId);

  /** Find users by role ID */
  List<User> findByRoleId(String roleId);

  /** Find all users (with pagination) */
  Page<User> findAll(Pageable pageable);

  /** Search users by keyword */
  Page<User> search(String keyword, Pageable pageable);

  /** Check if username exists */
  boolean existsByUsername(String username);

  /** Check if email exists */
  boolean existsByEmail(String email);

  /** Check if mobile exists */
  boolean existsByMobile(String mobile);

  /** Count users by department ID */
  long countByDeptId(String deptId);
}
