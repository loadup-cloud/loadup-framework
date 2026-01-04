package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.infrastructure.dataobject.UserDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * User JDBC Repository - Works with UserDO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public interface UserJdbcRepository extends CrudRepository<UserDO, String> {

  /**
   * Find user by username
   *
   * @param username username
   * @return user DO
   */
  @Query("SELECT * FROM upms_user WHERE username = :username AND deleted = false")
  Optional<UserDO> findByUsername(@Param("username") String username);

  /**
   * Find user by email
   *
   * @param email email
   * @return user DO
   */
  @Query("SELECT * FROM upms_user WHERE email = :email AND deleted = false")
  Optional<UserDO> findByEmail(@Param("email") String email);

  /**
   * Find user by phone
   *
   * @param phone phone
   * @return user DO
   */
  @Query("SELECT * FROM upms_user WHERE phone = :phone AND deleted = false")
  Optional<UserDO> findByPhone(@Param("phone") String phone);

  /**
   * Find users by department ID
   *
   * @param deptId department ID
   * @return user DO list
   */
  @Query("SELECT * FROM upms_user WHERE dept_id = :deptId AND deleted = false")
  List<UserDO> findByDeptId(@Param("deptId") String deptId);

  /**
   * Find users by role ID
   *
   * @param roleId role ID
   * @return user DO list
   */
  @Query(
      """
      SELECT u.* FROM upms_user u
      INNER JOIN upms_user_role ur ON u.id = ur.user_id
      WHERE ur.role_id = :roleId AND u.deleted = false
      """)
  List<UserDO> findByRoleId(@Param("roleId") String roleId);

  /**
   * Check if username exists
   *
   * @param username username
   * @return true if exists
   */
  @Query("SELECT COUNT(*) > 0 FROM upms_user WHERE username = :username AND deleted = false")
  boolean existsByUsername(@Param("username") String username);

  /**
   * Check if email exists
   *
   * @param email email
   * @return true if exists
   */
  @Query("SELECT COUNT(*) > 0 FROM upms_user WHERE email = :email AND deleted = false")
  boolean existsByEmail(@Param("email") String email);

  /**
   * Check if phone exists
   *
   * @param phone phone
   * @return true if exists
   */
  @Query("SELECT COUNT(*) > 0 FROM upms_user WHERE phone = :phone AND deleted = false")
  boolean existsByPhone(@Param("phone") String phone);

  /**
   * Count users by department
   *
   * @param deptId department ID
   * @return count
   */
  @Query("SELECT COUNT(*) FROM upms_user WHERE dept_id = :deptId AND deleted = false")
  long countByDeptId(@Param("deptId") String deptId);
}
