package com.github.loadup.modules.upms.domain.repository;

import com.github.loadup.modules.upms.domain.entity.Department;
import java.util.List;
import java.util.Optional;

/**
 * Department Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface DepartmentRepository {

  /** Save department */
  Department save(Department department);

  /** Update department */
  Department update(Department department);

  /** Delete department by ID */
  void deleteById(Long id);

  /** Find department by ID */
  Optional<Department> findById(Long id);

  /** Find department by code */
  Optional<Department> findByDeptCode(String deptCode);

  /** Find departments by parent ID */
  List<Department> findByParentId(Long parentId);

  /** Find all departments */
  List<Department> findAll();

  /** Find enabled departments */
  List<Department> findAllEnabled();

  /** Find root departments */
  List<Department> findRootDepartments();

  /** Check if department code exists */
  boolean existsByDeptCode(String deptCode);

  /** Check if department has children */
  boolean hasChildren(Long deptId);

  /** Check if department has users */
  boolean hasUsers(Long deptId);

  /** Build department tree */
  List<Department> buildTree();
}
