package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.Department;
import com.github.loadup.modules.upms.domain.repository.DepartmentRepository;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.JdbcDepartmentRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Department Repository Implementation
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class DepartmentRepositoryImpl implements DepartmentRepository {

  private final JdbcDepartmentRepository jdbcRepository;

  @Override
  public Department save(Department department) {
    return jdbcRepository.save(department);
  }

  @Override
  public Department update(Department department) {
    return jdbcRepository.save(department);
  }

  @Override
  public void deleteById(Long id) {
    // Use soft delete
    jdbcRepository.softDelete(id, 1L); // TODO: get actual operator ID
  }

  @Override
  public Optional<Department> findById(Long id) {
    return jdbcRepository.findById(id);
  }

  @Override
  public Optional<Department> findByDeptCode(String deptCode) {
    return jdbcRepository.findByDeptCode(deptCode);
  }

  @Override
  public List<Department> findByParentId(Long parentId) {
    return jdbcRepository.findByParentId(parentId);
  }

  @Override
  public List<Department> findAll() {
    return jdbcRepository.findAllActive();
  }

  @Override
  public List<Department> findAllEnabled() {
    return jdbcRepository.findAllEnabled();
  }

  @Override
  public List<Department> findRootDepartments() {
    return jdbcRepository.findRootDepartments();
  }

  @Override
  public boolean existsByDeptCode(String deptCode) {
    return jdbcRepository.countByDeptCode(deptCode) > 0;
  }

  @Override
  public boolean hasChildren(Long deptId) {
    return jdbcRepository.countChildren(deptId) > 0;
  }

  @Override
  public boolean hasUsers(Long deptId) {
    return jdbcRepository.countUsers(deptId) > 0;
  }

  @Override
  public List<Department> buildTree() {
    List<Department> allDepts = jdbcRepository.findAllActive();
    Map<Long, Department> deptMap = new HashMap<>();
    List<Department> roots = new ArrayList<>();

    // First pass: build map
    for (Department dept : allDepts) {
      deptMap.put(dept.getId(), dept);
      dept.setChildren(new ArrayList<>());
    }

    // Second pass: build tree structure
    for (Department dept : allDepts) {
      if (dept.isRoot()) {
        roots.add(dept);
      } else {
        Department parent = deptMap.get(dept.getParentId());
        if (parent != null) {
          parent.getChildren().add(dept);
          dept.setParent(parent);
        }
      }
    }

    return roots;
  }
}
