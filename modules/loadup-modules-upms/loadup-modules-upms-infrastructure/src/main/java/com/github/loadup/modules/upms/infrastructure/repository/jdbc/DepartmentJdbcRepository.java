package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.infrastructure.dataobject.DepartmentDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Department JDBC Repository
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public interface DepartmentJdbcRepository extends CrudRepository<DepartmentDO, String> {

  @Query("SELECT * FROM upms_department WHERE dept_code = :deptCode AND deleted = false")
  Optional<DepartmentDO> findByDeptCode(@Param("deptCode") String deptCode);

  @Query("SELECT * FROM upms_department WHERE parent_id = :parentId AND deleted = false")
  List<DepartmentDO> findByParentId(@Param("parentId") String parentId);

  @Query("SELECT COUNT(*) > 0 FROM upms_department WHERE dept_code = :deptCode AND deleted = false")
  boolean existsByDeptCode(@Param("deptCode") String deptCode);

  @Query("SELECT COUNT(*) > 0 FROM upms_user WHERE dept_id = :deptId AND deleted = false")
  boolean hasUsers(@Param("deptId") String deptId);

  @Query("SELECT COUNT(*) > 0 FROM upms_department WHERE parent_id = :parentId AND deleted = false")
  boolean hasChildren(@Param("parentId") String parentId);

  @Query("SELECT * FROM upms_department WHERE deleted = false ORDER BY sort_order")
  List<DepartmentDO> findAllOrderBySortOrder();

  @Query("SELECT * FROM upms_department WHERE status = 1 AND deleted = false ORDER BY sort_order")
  List<DepartmentDO> findAllEnabled();
}
