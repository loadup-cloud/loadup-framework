package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.Permission;
import com.github.loadup.modules.upms.domain.repository.PermissionRepository;
import com.github.loadup.modules.upms.infrastructure.dataobject.PermissionDO;
import com.github.loadup.modules.upms.infrastructure.mapper.PermissionMapper;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.PermissionJdbcRepository;
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

  private final PermissionJdbcRepository jdbcRepository;
  private final PermissionMapper permissionMapper;

  @Override
  public Permission save(Permission permission) {
    PermissionDO permissionDO = permissionMapper.toDO(permission);
    PermissionDO saved = jdbcRepository.save(permissionDO);
    return permissionMapper.toEntity(saved);
  }

  @Override
  public Permission update(Permission permission) {
    PermissionDO permissionDO = permissionMapper.toDO(permission);
    PermissionDO updated = jdbcRepository.save(permissionDO);
    return permissionMapper.toEntity(updated);
  }

  @Override
  public void deleteById(String id) {
    jdbcRepository
        .findById(id)
        .ifPresent(
            permissionDO -> {
              permissionDO.setDeleted(true);
              jdbcRepository.save(permissionDO);
            });
  }

  @Override
  public Optional<Permission> findById(String id) {
    return jdbcRepository.findById(id).map(permissionMapper::toEntity);
  }

  @Override
  public Optional<Permission> findByPermissionCode(String permissionCode) {
    return jdbcRepository.findByPermissionCode(permissionCode).map(permissionMapper::toEntity);
  }

  @Override
  public List<Permission> findByParentId(String parentId) {
    List<PermissionDO> permissionDOList = jdbcRepository.findByParentId(parentId);
    return permissionMapper.toEntityList(permissionDOList);
  }

  @Override
  public List<Permission> findByPermissionType(Short permissionType) {
    List<PermissionDO> permissionDOList = jdbcRepository.findByPermissionType(permissionType);
    return permissionMapper.toEntityList(permissionDOList);
  }

  @Override
  public List<Permission> findByRoleId(String roleId) {
    List<PermissionDO> permissionDOList = jdbcRepository.findByRoleId(roleId);
    return permissionMapper.toEntityList(permissionDOList);
  }

  @Override
  public List<Permission> findByUserId(String userId) {
    List<PermissionDO> permissionDOList = jdbcRepository.findByUserId(userId);
    return permissionMapper.toEntityList(permissionDOList);
  }

  @Override
  public List<String> getRolePermissionIds(String roleId) {
    return jdbcRepository.getRolePermissionIds(roleId);
  }

  @Override
  public List<Permission> findAll() {
    List<PermissionDO> permissionDOList = jdbcRepository.findAllOrderBySortOrder();
    return permissionMapper.toEntityList(permissionDOList);
  }

  @Override
  public List<Permission> findAllEnabled() {
    List<PermissionDO> permissionDOList = jdbcRepository.findAllEnabled();
    return permissionMapper.toEntityList(permissionDOList);
  }

  @Override
  public List<Permission> findMenuPermissions() {
    List<PermissionDO> permissionDOList = jdbcRepository.findByPermissionType((short) 1);
    return permissionMapper.toEntityList(permissionDOList);
  }

  @Override
  public boolean existsByPermissionCode(String permissionCode) {
    return jdbcRepository.existsByPermissionCode(permissionCode);
  }

  @Override
  public void removeAllPermissionsFromRole(String roleId) {
    // TODO: implement with JdbcTemplate
    // DELETE FROM upms_role_permission WHERE role_id = :roleId
  }

  @Override
  public void removePermissionFromRole(String roleId, String permissionId) {
    // TODO: implement with JdbcTemplate
    // DELETE FROM upms_role_permission WHERE role_id = ? AND permission_id = ?
  }

  @Override
  public void assignPermissionToRole(String roleId, String permissionId, String operatorId) {
    // TODO: implement with JdbcTemplate
    // INSERT INTO upms_role_permission (role_id, permission_id, created_by, created_time)
    // VALUES (?, ?, ?, NOW())
  }

  @Override
  public void batchAssignPermissionsToRole(
      String roleId, List<String> permissionIds, String operatorId) {
    // TODO: implement with JdbcTemplate
    // INSERT INTO upms_role_permission (role_id, permission_id, created_by, created_time)
    // VALUES (?, ?, ?, NOW()) for each permissionId
  }
}
