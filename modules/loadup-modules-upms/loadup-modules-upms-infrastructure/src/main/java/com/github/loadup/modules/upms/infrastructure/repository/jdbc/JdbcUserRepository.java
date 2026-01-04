package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.domain.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JDBC User Repository
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface JdbcUserRepository
    extends CrudRepository<User, Long>, PagingAndSortingRepository<User, Long> {

  @Query("SELECT * FROM upms_user WHERE username = :username AND deleted = false")
  Optional<User> findByUsername(@Param("username") String username);

  @Query("SELECT * FROM upms_user WHERE email = :email AND deleted = false")
  Optional<User> findByEmail(@Param("email") String email);

  @Query("SELECT * FROM upms_user WHERE phone = :phone AND deleted = false")
  Optional<User> findByPhone(@Param("phone") String phone);

  @Query("SELECT * FROM upms_user WHERE dept_id = :deptId AND deleted = false")
  List<User> findByDeptId(@Param("deptId") String deptId);

  @Query(
      """
        SELECT u.* FROM upms_user u
        INNER JOIN upms_user_role ur ON u.id = ur.user_id
        WHERE ur.role_id = :roleId AND u.deleted = false
    """)
  List<User> findByRoleId(@Param("roleId") String roleId);

  @Query(
      """
        SELECT * FROM upms_user
        WHERE deleted = false
        AND (username LIKE CONCAT('%', :keyword, '%')
            OR nickname LIKE CONCAT('%', :keyword, '%')
            OR real_name LIKE CONCAT('%', :keyword, '%'))
    """)
  List<User> searchByKeyword(@Param("keyword") String keyword);

  @Query("SELECT COUNT(*) FROM upms_user WHERE username = :username AND deleted = false")
  long countByUsername(@Param("username") String username);

  @Query("SELECT COUNT(*) FROM upms_user WHERE email = :email AND deleted = false")
  long countByEmail(@Param("email") String email);

  @Query("SELECT COUNT(*) FROM upms_user WHERE phone = :phone AND deleted = false")
  long countByPhone(@Param("phone") String phone);

  @Query("SELECT COUNT(*) FROM upms_user WHERE dept_id = :deptId AND deleted = false")
  long countByDeptId(@Param("deptId") String deptId);

  @Modifying
  @Query(
      "UPDATE upms_user SET deleted = true, updated_by = :updatedBy, updated_time = CURRENT_TIMESTAMP WHERE id = :id")
  void softDelete(@Param("id") Long id, @Param("updatedBy") String updatedBy);

  @Query("SELECT * FROM upms_user WHERE deleted = false ORDER BY created_time DESC")
  List<User> findAllActive();
}
