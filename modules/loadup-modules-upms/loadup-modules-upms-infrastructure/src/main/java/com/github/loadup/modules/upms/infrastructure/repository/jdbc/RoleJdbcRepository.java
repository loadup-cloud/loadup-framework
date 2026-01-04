package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.infrastructure.dataobject.RoleDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Role JDBC Repository
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public interface RoleJdbcRepository extends CrudRepository<RoleDO, String> {

  @Query("SELECT * FROM upms_role WHERE role_code = :roleCode AND deleted = false")
  Optional<RoleDO> findByRoleCode(@Param("roleCode") String roleCode);

  @Query("SELECT * FROM upms_role WHERE parent_role_id = :parentRoleId AND deleted = false")
  List<RoleDO> findByParentRoleId(@Param("parentRoleId") String parentRoleId);

  @Query(
      """
      SELECT r.* FROM upms_role r
      INNER JOIN upms_user_role ur ON r.id = ur.role_id
      WHERE ur.user_id = :userId AND r.deleted = false
      """)
  List<RoleDO> findByUserId(@Param("userId") String userId);

  @Query("SELECT COUNT(*) > 0 FROM upms_role WHERE role_code = :roleCode AND deleted = false")
  boolean existsByRoleCode(@Param("roleCode") String roleCode);

  @Query(
      """
      SELECT COUNT(*) FROM upms_user_role ur
      INNER JOIN upms_user u ON ur.user_id = u.id
      WHERE ur.role_id = :roleId AND u.deleted = false
      """)
  long countUsersByRoleId(@Param("roleId") String roleId);

  @Query("SELECT * FROM upms_role WHERE deleted = false ORDER BY sort_order")
  List<RoleDO> findAllOrderBySortOrder();

  @Query("SELECT * FROM upms_role WHERE status = 1 AND deleted = false ORDER BY sort_order")
  List<RoleDO> findAllEnabled();

  @Query("SELECT dept_id FROM upms_role_department WHERE role_id = :roleId")
  List<String> findDepartmentIdsByRoleId(@Param("roleId") String roleId);

  @Query("SELECT role_id FROM upms_user_role WHERE user_id = :userId")
  List<String> getUserRoleIds(@Param("userId") String userId);
}
