package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.domain.entity.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JDBC Department Repository
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface JdbcDepartmentRepository
    extends CrudRepository<Department, Long>, PagingAndSortingRepository<Department, Long> {

  @Query("SELECT * FROM upms_department WHERE dept_code = :deptCode AND deleted = false")
  Optional<Department> findByDeptCode(@Param("deptCode") String deptCode);

  @Query(
      "SELECT * FROM upms_department WHERE parent_id = :parentId AND deleted = false ORDER BY sort_order")
  List<Department> findByParentId(@Param("parentId") String parentId);

  @Query("SELECT * FROM upms_department WHERE deleted = false ORDER BY sort_order, created_time")
  List<Department> findAllActive();

  @Query(
      "SELECT * FROM upms_department WHERE status = 1 AND deleted = false ORDER BY sort_order, created_time")
  List<Department> findAllEnabled();

  @Query(
      "SELECT * FROM upms_department WHERE (parent_id = 0 OR parent_id IS NULL) AND deleted = false ORDER BY sort_order")
  List<Department> findRootDepartments();

  @Query("SELECT COUNT(*) FROM upms_department WHERE dept_code = :deptCode AND deleted = false")
  long countByDeptCode(@Param("deptCode") String deptCode);

  @Query("SELECT COUNT(*) FROM upms_department WHERE parent_id = :deptId AND deleted = false")
  long countChildren(@Param("deptId") String deptId);

  @Query("SELECT COUNT(*) FROM upms_user WHERE dept_id = :deptId AND deleted = false")
  long countUsers(@Param("deptId") String deptId);

  @Modifying
  @Query(
      "UPDATE upms_department SET deleted = true, updated_by = :updatedBy, updated_time = CURRENT_TIMESTAMP WHERE id = :id")
  void softDelete(@Param("id") Long id, @Param("updatedBy") String updatedBy);
}
