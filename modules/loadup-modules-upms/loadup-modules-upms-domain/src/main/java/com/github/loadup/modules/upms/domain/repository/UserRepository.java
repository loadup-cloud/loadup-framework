package com.github.loadup.modules.upms.domain.repository;

import com.github.loadup.modules.upms.domain.entity.User;
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
public interface UserRepository {

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
  Optional<User> findByPhone(String phone);

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

  /** Check if phone exists */
  boolean existsByPhone(String phone);

  /** Count users by department ID */
  long countByDeptId(String deptId);
}
