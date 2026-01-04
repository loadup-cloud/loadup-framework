package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.domain.entity.Permission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JDBC Permission Repository
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface JdbcPermissionRepository
    extends CrudRepository<Permission, Long>, PagingAndSortingRepository<Permission, Long> {

  @Query(
      "SELECT * FROM upms_permission WHERE permission_code = :permissionCode AND deleted = false")
  Optional<Permission> findByPermissionCode(@Param("permissionCode") String permissionCode);

  @Query(
      """
        SELECT p.* FROM upms_permission p
        INNER JOIN upms_role_permission rp ON p.id = rp.permission_id
        INNER JOIN upms_user_role ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = :userId AND p.deleted = false
    """)
  List<Permission> findByUserId(@Param("userId") Long userId);

  @Query(
      """
        SELECT p.* FROM upms_permission p
        INNER JOIN upms_role_permission rp ON p.id = rp.permission_id
        WHERE rp.role_id = :roleId AND p.deleted = false
    """)
  List<Permission> findByRoleId(@Param("roleId") Long roleId);

  @Query(
      "SELECT * FROM upms_permission WHERE parent_id = :parentId AND deleted = false ORDER BY sort_order")
  List<Permission> findByParentId(@Param("parentId") Long parentId);

  @Query(
      "SELECT * FROM upms_permission WHERE permission_type = :permissionType AND deleted = false ORDER BY sort_order, created_time")
  List<Permission> findByPermissionType(@Param("permissionType") Short permissionType);

  @Query("SELECT * FROM upms_permission WHERE deleted = false ORDER BY sort_order, created_time")
  List<Permission> findAllActive();

  @Query(
      "SELECT * FROM upms_permission WHERE status = 1 AND deleted = false ORDER BY sort_order, created_time")
  List<Permission> findAllEnabled();

  @Query(
      """
        SELECT * FROM upms_permission
        WHERE permission_type = 1 AND status = 1 AND deleted = false
        ORDER BY sort_order, created_time
    """)
  List<Permission> findMenuPermissions();

  @Query(
      "SELECT COUNT(*) FROM upms_permission WHERE permission_code = :permissionCode AND deleted = false")
  long countByPermissionCode(@Param("permissionCode") String permissionCode);

  @Query("SELECT permission_id FROM upms_role_permission WHERE role_id = :roleId")
  List<Long> getRolePermissionIds(@Param("roleId") Long roleId);

  @Modifying
  @Query(
      """
        INSERT INTO upms_role_permission (role_id, permission_id, created_by, created_time)
        VALUES (:roleId, :permissionId, :operatorId, CURRENT_TIMESTAMP)
    """)
  void assignPermissionToRole(
      @Param("roleId") Long roleId,
      @Param("permissionId") Long permissionId,
      @Param("operatorId") Long operatorId);

  @Modifying
  @Query(
      "DELETE FROM upms_role_permission WHERE role_id = :roleId AND permission_id = :permissionId")
  void removePermissionFromRole(
      @Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

  @Modifying
  @Query("DELETE FROM upms_role_permission WHERE role_id = :roleId")
  void removeAllPermissionsFromRole(@Param("roleId") Long roleId);

  @Modifying
  @Query(
      "UPDATE upms_permission SET deleted = true, updated_by = :updatedBy, updated_time = CURRENT_TIMESTAMP WHERE id = :id")
  void softDelete(@Param("id") Long id, @Param("updatedBy") Long updatedBy);
}
