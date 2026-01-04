package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.Department;
import com.github.loadup.modules.upms.domain.repository.DepartmentRepository;
import com.github.loadup.modules.upms.infrastructure.dataobject.DepartmentDO;
import com.github.loadup.modules.upms.infrastructure.mapper.DepartmentMapper;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.DepartmentJdbcRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

  private final DepartmentJdbcRepository jdbcRepository;
  private final DepartmentMapper departmentMapper;

  @Override
  public Department save(Department department) {
    DepartmentDO departmentDO = departmentMapper.toDO(department);
    DepartmentDO saved = jdbcRepository.save(departmentDO);
    return departmentMapper.toEntity(saved);
  }

  @Override
  public Department update(Department department) {
    DepartmentDO departmentDO = departmentMapper.toDO(department);
    DepartmentDO updated = jdbcRepository.save(departmentDO);
    return departmentMapper.toEntity(updated);
  }

  @Override
  public void deleteById(String id) {
    jdbcRepository
        .findById(id)
        .ifPresent(
            departmentDO -> {
              departmentDO.setDeleted(true);
              jdbcRepository.save(departmentDO);
            });
  }

  @Override
  public Optional<Department> findById(String id) {
    return jdbcRepository.findById(id).map(departmentMapper::toEntity);
  }

  @Override
  public Optional<Department> findByDeptCode(String deptCode) {
    return jdbcRepository.findByDeptCode(deptCode).map(departmentMapper::toEntity);
  }

  @Override
  public List<Department> findByParentId(String parentId) {
    List<DepartmentDO> departmentDOList = jdbcRepository.findByParentId(parentId);
    return departmentMapper.toEntityList(departmentDOList);
  }

  @Override
  public boolean existsByDeptCode(String deptCode) {
    return jdbcRepository.existsByDeptCode(deptCode);
  }

  @Override
  public List<Department> findAll() {
    List<DepartmentDO> departmentDOList = jdbcRepository.findAllOrderBySortOrder();
    return departmentMapper.toEntityList(departmentDOList);
  }

  @Override
  public List<Department> findRootDepartments() {
    return findByParentId("0");
  }

  @Override
  public List<Department> findAllEnabled() {
    List<DepartmentDO> departmentDOList = jdbcRepository.findAllEnabled();
    return departmentMapper.toEntityList(departmentDOList);
  }

  @Override
  public boolean hasUsers(String deptId) {
    return jdbcRepository.hasUsers(deptId);
  }

  @Override
  public boolean hasChildren(String parentId) {
    return jdbcRepository.hasChildren(parentId);
  }

  @Override
  public List<Department> buildTree() {
    List<DepartmentDO> allDepts = jdbcRepository.findAllOrderBySortOrder();
    List<Department> departments = departmentMapper.toEntityList(allDepts);
    return buildTreeRecursive(departments, "0");
  }

  private List<Department> buildTreeRecursive(List<Department> all, String parentId) {
    return all.stream()
        .filter(
            d -> {
              if ("0".equals(parentId)) {
                return d.getParentId() == null || "0".equals(d.getParentId());
              }
              return parentId.equals(d.getParentId());
            })
        .peek(d -> d.setChildren(buildTreeRecursive(all, d.getId())))
        .collect(Collectors.toList());
  }
}
