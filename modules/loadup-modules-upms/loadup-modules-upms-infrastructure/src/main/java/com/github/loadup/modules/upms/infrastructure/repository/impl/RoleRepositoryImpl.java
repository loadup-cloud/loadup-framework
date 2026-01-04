package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.domain.repository.RoleRepository;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.JdbcRoleRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Role Repository Implementation
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

  private final JdbcRoleRepository jdbcRepository;

  @Override
  public Role save(Role role) {
    return jdbcRepository.save(role);
  }

  @Override
  public Role update(Role role) {
    return jdbcRepository.save(role);
  }

  @Override
  public void deleteById(Long id) {
    // Use soft delete
    jdbcRepository.softDelete(id, 1L); // TODO: get actual operator ID
  }

  @Override
  public Optional<Role> findById(Long id) {
    return jdbcRepository.findById(id);
  }

  @Override
  public Optional<Role> findByRoleCode(String roleCode) {
    return jdbcRepository.findByRoleCode(roleCode);
  }

  @Override
  public List<Role> findByUserId(Long userId) {
    return jdbcRepository.findByUserId(userId);
  }

  @Override
  public List<Role> findByParentRoleId(Long parentRoleId) {
    return jdbcRepository.findByParentRoleId(parentRoleId);
  }

  @Override
  public List<Role> findAll() {
    return jdbcRepository.findAllActive();
  }

  @Override
  public List<Role> findAllEnabled() {
    return jdbcRepository.findAllEnabled();
  }

  @Override
  public boolean existsByRoleCode(String roleCode) {
    return jdbcRepository.countByRoleCode(roleCode) > 0;
  }

  @Override
  public void assignRoleToUser(Long userId, Long roleId, Long operatorId) {
    jdbcRepository.assignRoleToUser(userId, roleId, operatorId);
  }

  @Override
  public void removeRoleFromUser(Long userId, Long roleId) {
    jdbcRepository.removeRoleFromUser(userId, roleId);
  }

  @Override
  public List<Long> getUserRoleIds(Long userId) {
    return jdbcRepository.getUserRoleIds(userId);
  }

  @Override
  public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
    for (Long permissionId : permissionIds) {
      jdbcRepository.assignPermissionToRole(roleId, permissionId);
    }
  }

  @Override
  public void removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
    for (Long permissionId : permissionIds) {
      jdbcRepository.removePermissionFromRole(roleId, permissionId);
    }
  }

  @Override
  public void assignDepartmentsToRole(Long roleId, List<Long> departmentIds) {
    for (Long deptId : departmentIds) {
      jdbcRepository.assignDepartmentToRole(roleId, deptId);
    }
  }

  @Override
  public void removeDepartmentsFromRole(Long roleId, List<Long> departmentIds) {
    for (Long deptId : departmentIds) {
      jdbcRepository.removeDepartmentFromRole(roleId, deptId);
    }
  }

  @Override
  public List<Long> findDepartmentIdsByRoleId(Long roleId) {
    return jdbcRepository.findDepartmentIdsByRoleId(roleId);
  }

  @Override
  public long countUsersByRoleId(Long roleId) {
    return jdbcRepository.countUsersByRoleId(roleId);
  }
}
