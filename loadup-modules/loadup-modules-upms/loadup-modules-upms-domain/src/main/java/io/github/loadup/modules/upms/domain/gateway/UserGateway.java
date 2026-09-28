package io.github.loadup.modules.upms.domain.gateway;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.dto.PageQuery;
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
     * Find all users (with pagination)
     */
    PageResult<User> findAll(PageQuery query);

    /**
     * Search users by keyword
     */
    PageResult<User> search(String keyword, PageQuery query);

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
