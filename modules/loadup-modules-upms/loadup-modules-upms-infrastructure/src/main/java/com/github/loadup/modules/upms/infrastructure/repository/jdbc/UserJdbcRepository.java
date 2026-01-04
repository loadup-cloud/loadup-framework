package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.infrastructure.dataobject.UserDO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * User JDBC Repository - Works with UserDO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public interface UserJdbcRepository
    extends PagingAndSortingRepository<UserDO, String>, CrudRepository<UserDO, String> {

  /**
   * Find user by username
   *
   * @param username username
   * @return user DO
   */
  @Query("SELECT * FROM upms_user WHERE username = :username AND deleted = false")
  Optional<UserDO> findByUsername(@Param("username") String username);

  @Query("SELECT * FROM upms_user WHERE email = :email AND deleted = false")
  Optional<UserDO> findByEmail(@Param("email") String email);

  @Query("SELECT * FROM upms_user WHERE phone = :phone AND deleted = false")
  Optional<UserDO> findByPhone(@Param("phone") String phone);

  @Query("SELECT * FROM upms_user WHERE dept_id = :deptId AND deleted = false")
  java.util.List<UserDO> findByDeptId(@Param("deptId") String deptId);

  @Query(
      """
      SELECT u.* FROM upms_user u
      INNER JOIN upms_user_role ur ON u.id = ur.user_id
      WHERE ur.role_id = :roleId AND u.deleted = false
      """)
  java.util.List<UserDO> findByRoleId(@Param("roleId") String roleId);

  @Query("SELECT COUNT(*) > 0 FROM upms_user WHERE username = :username AND deleted = false")
  boolean existsByUsername(@Param("username") String username);

  @Query("SELECT COUNT(*) > 0 FROM upms_user WHERE email = :email AND deleted = false")
  boolean existsByEmail(@Param("email") String email);

  @Query("SELECT COUNT(*) > 0 FROM upms_user WHERE phone = :phone AND deleted = false")
  boolean existsByPhone(@Param("phone") String phone);

  @Query("SELECT COUNT(*) FROM upms_user WHERE dept_id = :deptId AND deleted = false")
  long countByDeptId(@Param("deptId") String deptId);

  /**
   * Search users by keyword with pagination
   *
   * @param keyword keyword to search (username, nickname, email, phone)
   * @param pageable pageable
   * @return user DO page
   */
  @Query(
      """
      SELECT * FROM upms_user
      WHERE deleted = false
      AND (username LIKE CONCAT('%', :keyword, '%')
           OR nickname LIKE CONCAT('%', :keyword, '%')
           OR email LIKE CONCAT('%', :keyword, '%')
           OR phone LIKE CONCAT('%', :keyword, '%'))
      ORDER BY created_time DESC
      """)
  Page<UserDO> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
