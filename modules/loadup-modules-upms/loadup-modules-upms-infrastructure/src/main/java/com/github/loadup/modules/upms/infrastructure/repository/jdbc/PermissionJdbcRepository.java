package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.infrastructure.dataobject.PermissionDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Permission JDBC Repository
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public interface PermissionJdbcRepository extends CrudRepository<PermissionDO, String> {

  @Query("SELECT * FROM upms_permission WHERE parent_id = :parentId AND deleted = false")
  List<PermissionDO> findByParentId(@Param("parentId") String parentId);

  @Query(
      "SELECT * FROM upms_permission WHERE permission_code = :permissionCode AND deleted = false")
  Optional<PermissionDO> findByPermissionCode(@Param("permissionCode") String permissionCode);

  @Query(
      "SELECT * FROM upms_permission WHERE permission_type = :permissionType AND deleted = false")
  List<PermissionDO> findByPermissionType(@Param("permissionType") Short permissionType);

  @Query(
      """
      SELECT p.* FROM upms_permission p
      INNER JOIN upms_role_permission rp ON p.id = rp.permission_id
      WHERE rp.role_id = :roleId AND p.deleted = false
      """)
  List<PermissionDO> findByRoleId(@Param("roleId") String roleId);

  @Query(
      """
      SELECT p.* FROM upms_permission p
      INNER JOIN upms_role_permission rp ON p.id = rp.permission_id
      INNER JOIN upms_user_role ur ON rp.role_id = ur.role_id
      WHERE ur.user_id = :userId AND p.deleted = false
      """)
  List<PermissionDO> findByUserId(@Param("userId") String userId);

  @Query(
      """
      SELECT permission_id FROM upms_role_permission
      WHERE role_id = :roleId
      """)
  List<String> getRolePermissionIds(@Param("roleId") String roleId);

  @Query("SELECT * FROM upms_permission WHERE deleted = false ORDER BY sort_order")
  List<PermissionDO> findAllOrderBySortOrder();

  @Query(
      "SELECT COUNT(*) > 0 FROM upms_permission WHERE permission_code = :permissionCode AND"
          + " deleted = false")
  boolean existsByPermissionCode(@Param("permissionCode") String permissionCode);

  @Query("SELECT * FROM upms_permission WHERE status = 1 AND deleted = false ORDER BY sort_order")
  List<PermissionDO> findAllEnabled();
}
