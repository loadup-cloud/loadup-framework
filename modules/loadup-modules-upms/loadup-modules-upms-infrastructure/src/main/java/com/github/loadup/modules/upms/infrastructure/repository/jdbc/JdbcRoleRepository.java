package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.domain.entity.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JDBC Role Repository
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface JdbcRoleRepository
    extends CrudRepository<Role, Long>, PagingAndSortingRepository<Role, Long> {

  @Query("SELECT * FROM upms_role WHERE role_code = :roleCode AND deleted = false")
  Optional<Role> findByRoleCode(@Param("roleCode") String roleCode);

  @Query(
      """
        SELECT r.* FROM upms_role r
        INNER JOIN upms_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = :userId AND r.deleted = false
    """)
  List<Role> findByUserId(@Param("userId") String userId);

  @Query("SELECT * FROM upms_role WHERE parent_role_id = :parentRoleId AND deleted = false")
  List<Role> findByParentRoleId(@Param("parentRoleId") Long parentRoleId);

  @Query("SELECT * FROM upms_role WHERE deleted = false ORDER BY sort_order, created_time DESC")
  List<Role> findAllActive();

  @Query(
      "SELECT * FROM upms_role WHERE status = 1 AND deleted = false ORDER BY sort_order, created_time DESC")
  List<Role> findAllEnabled();

  @Query("SELECT COUNT(*) FROM upms_role WHERE role_code = :roleCode AND deleted = false")
  long countByRoleCode(@Param("roleCode") String roleCode);

  @Query("SELECT role_id FROM upms_user_role WHERE user_id = :userId")
  List<Long> getUserRoleIds(@Param("userId") String userId);

  @Modifying
  @Query(
      """
        INSERT INTO upms_user_role (user_id, role_id, created_by, created_time)
        VALUES (:userId, :roleId, :operatorId, CURRENT_TIMESTAMP)
    """)
  void assignRoleToUser(
      @Param("userId") Long userId,
      @Param("roleId") Long roleId,
      @Param("operatorId") Long operatorId);

  @Modifying
  @Query("DELETE FROM upms_user_role WHERE user_id = :userId AND role_id = :roleId")
  void removeRoleFromUser(@Param("userId") Long userId, @Param("roleId") String roleId);

  @Modifying
  @Query(
      "UPDATE upms_role SET deleted = true, updated_by = :updatedBy, updated_time = CURRENT_TIMESTAMP WHERE id = :id")
  void softDelete(@Param("id") Long id, @Param("updatedBy") String updatedBy);

  @Modifying
  @Query(
      """
        INSERT INTO upms_role_permission (role_id, permission_id, created_time)
        VALUES (:roleId, :permissionId, CURRENT_TIMESTAMP)
    """)
  void assignPermissionToRole(
      @Param("roleId") Long roleId, @Param("permissionId") String permissionId);

  @Modifying
  @Query(
      "DELETE FROM upms_role_permission WHERE role_id = :roleId AND permission_id = :permissionId")
  void removePermissionFromRole(
      @Param("roleId") Long roleId, @Param("permissionId") String permissionId);

  @Modifying
  @Query(
      """
        INSERT INTO upms_role_department (role_id, dept_id, created_time)
        VALUES (:roleId, :deptId, CURRENT_TIMESTAMP)
    """)
  void assignDepartmentToRole(@Param("roleId") Long roleId, @Param("deptId") String deptId);

  @Modifying
  @Query("DELETE FROM upms_role_department WHERE role_id = :roleId AND dept_id = :deptId")
  void removeDepartmentFromRole(@Param("roleId") Long roleId, @Param("deptId") String deptId);

  @Query("SELECT dept_id FROM upms_role_department WHERE role_id = :roleId")
  List<String> findDepartmentIdsByRoleId(@Param("roleId") String roleId);

  @Query("SELECT COUNT(*) FROM upms_user_role WHERE role_id = :roleId")
  long countUsersByRoleId(@Param("roleId") String roleId);
}
