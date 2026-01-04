package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.domain.repository.RoleRepository;
import com.github.loadup.modules.upms.infrastructure.dataobject.RoleDO;
import com.github.loadup.modules.upms.infrastructure.mapper.RoleMapper;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.RoleJdbcRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

  private final RoleJdbcRepository jdbcRepository;
  private final RoleMapper roleMapper;

  @Override
  public Role save(Role role) {
    RoleDO roleDO = roleMapper.toDO(role);
    RoleDO saved = jdbcRepository.save(roleDO);
    return roleMapper.toEntity(saved);
  }

  @Override
  public Role update(Role role) {
    RoleDO roleDO = roleMapper.toDO(role);
    RoleDO updated = jdbcRepository.save(roleDO);
    return roleMapper.toEntity(updated);
  }

  @Override
  public void deleteById(String id) {
    jdbcRepository
        .findById(id)
        .ifPresent(
            roleDO -> {
              roleDO.setDeleted(true);
              jdbcRepository.save(roleDO);
            });
  }

  @Override
  public Optional<Role> findById(String id) {
    return jdbcRepository.findById(id).map(roleMapper::toEntity);
  }

  @Override
  public Optional<Role> findByRoleCode(String roleCode) {
    return jdbcRepository.findByRoleCode(roleCode).map(roleMapper::toEntity);
  }

  @Override
  public List<Role> findByParentRoleId(String parentRoleId) {
    List<RoleDO> roleDOList = jdbcRepository.findByParentRoleId(parentRoleId);
    return roleMapper.toEntityList(roleDOList);
  }

  @Override
  public List<Role> findByUserId(String userId) {
    List<RoleDO> roleDOList = jdbcRepository.findByUserId(userId);
    return roleMapper.toEntityList(roleDOList);
  }

  @Override
  public boolean existsByRoleCode(String roleCode) {
    return jdbcRepository.existsByRoleCode(roleCode);
  }

  @Override
  public List<Role> findAll() {
    List<RoleDO> roleDOList = jdbcRepository.findAllOrderBySortOrder();
    return roleMapper.toEntityList(roleDOList);
  }

  @Override
  public List<Role> findAllEnabled() {
    List<RoleDO> roleDOList = jdbcRepository.findAllEnabled();
    return roleMapper.toEntityList(roleDOList);
  }

  @Override
  public Page<Role> findAll(Pageable pageable) {
    Page<RoleDO> roleDOPage = jdbcRepository.findAll(pageable);
    List<Role> roles = roleMapper.toEntityList(roleDOPage.getContent());
    return new PageImpl<>(roles, pageable, roleDOPage.getTotalElements());
  }

  @Override
  public long countUsersByRoleId(String roleId) {
    return jdbcRepository.countUsersByRoleId(roleId);
  }

  @Override
  public List<String> findDepartmentIdsByRoleId(String roleId) {
    return jdbcRepository.findDepartmentIdsByRoleId(roleId);
  }

  @Override
  public List<String> getUserRoleIds(String userId) {
    return jdbcRepository.getUserRoleIds(userId);
  }

  @Override
  public void removeRoleFromUser(String userId, String roleId) {
    jdbcRepository.deleteUserRole(userId, roleId);
  }

  @Override
  public void assignRoleToUser(String userId, String roleId, String operatorId) {
    jdbcRepository.insertUserRole(userId, roleId, operatorId);
  }

  @Override
  public void assignDepartmentsToRole(String roleId, List<String> departmentIds) {
    // Use system as default operator if not provided
    String operatorId = "system";
    for (String deptId : departmentIds) {
      jdbcRepository.insertRoleDepartment(roleId, deptId, operatorId);
    }
  }

  @Override
  public void assignPermissionsToRole(String roleId, List<String> permissionIds) {
    String operatorId = "system";
    for (String permissionId : permissionIds) {
      jdbcRepository.insertRolePermission(roleId, permissionId, operatorId);
    }
  }

  @Override
  public void removePermissionsFromRole(String roleId, List<String> permissionIds) {
    for (String permissionId : permissionIds) {
      jdbcRepository.deleteRolePermission(roleId, permissionId);
    }
  }

  @Override
  public void removeDepartmentsFromRole(String roleId, List<String> departmentIds) {
    for (String deptId : departmentIds) {
      jdbcRepository.deleteRoleDepartment(roleId, deptId);
    }
  }
}
