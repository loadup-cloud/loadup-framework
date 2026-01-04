package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.Permission;
import com.github.loadup.modules.upms.domain.repository.PermissionRepository;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.JdbcPermissionRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Permission Repository Implementation
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

  private final JdbcPermissionRepository jdbcRepository;

  @Override
  public Permission save(Permission permission) {
    return jdbcRepository.save(permission);
  }

  @Override
  public Permission update(Permission permission) {
    return jdbcRepository.save(permission);
  }

  @Override
  public void deleteById(Long id) {
    // Use soft delete
    jdbcRepository.softDelete(id, 1L); // TODO: get actual operator ID
  }

  @Override
  public Optional<Permission> findById(Long id) {
    return jdbcRepository.findById(id);
  }

  @Override
  public Optional<Permission> findByPermissionCode(String permissionCode) {
    return jdbcRepository.findByPermissionCode(permissionCode);
  }

  @Override
  public List<Permission> findByUserId(Long userId) {
    return jdbcRepository.findByUserId(userId);
  }

  @Override
  public List<Permission> findByRoleId(Long roleId) {
    return jdbcRepository.findByRoleId(roleId);
  }

  @Override
  public List<Permission> findByParentId(Long parentId) {
    return jdbcRepository.findByParentId(parentId);
  }

  @Override
  public List<Permission> findByPermissionType(Short permissionType) {
    return jdbcRepository.findByPermissionType(permissionType);
  }

  @Override
  public List<Permission> findAll() {
    return jdbcRepository.findAllActive();
  }

  @Override
  public List<Permission> findAllEnabled() {
    return jdbcRepository.findAllEnabled();
  }

  @Override
  public List<Permission> findMenuPermissions() {
    return jdbcRepository.findMenuPermissions();
  }

  @Override
  public boolean existsByPermissionCode(String permissionCode) {
    return jdbcRepository.countByPermissionCode(permissionCode) > 0;
  }

  @Override
  public void assignPermissionToRole(Long roleId, Long permissionId, Long operatorId) {
    jdbcRepository.assignPermissionToRole(roleId, permissionId, operatorId);
  }

  @Override
  public void removePermissionFromRole(Long roleId, Long permissionId) {
    jdbcRepository.removePermissionFromRole(roleId, permissionId);
  }

  @Override
  public void batchAssignPermissionsToRole(Long roleId, List<Long> permissionIds, Long operatorId) {
    // First remove all existing permissions
    jdbcRepository.removeAllPermissionsFromRole(roleId);
    // Then add new permissions
    for (Long permissionId : permissionIds) {
      jdbcRepository.assignPermissionToRole(roleId, permissionId, operatorId);
    }
  }

  @Override
  public void removeAllPermissionsFromRole(Long roleId) {
    jdbcRepository.removeAllPermissionsFromRole(roleId);
  }

  @Override
  public List<Long> getRolePermissionIds(Long roleId) {
    return jdbcRepository.getRolePermissionIds(roleId);
  }
}
