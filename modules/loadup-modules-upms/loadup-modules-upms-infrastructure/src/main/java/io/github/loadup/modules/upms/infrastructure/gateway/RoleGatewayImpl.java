package io.github.loadup.modules.upms.infrastructure.gateway;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.ROLE_DO;

import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.infrastructure.converter.RoleConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.RoleDO;
import io.github.loadup.modules.upms.infrastructure.mapper.RoleDOMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Role Repository Implementation using MyBatis-Flex
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class RoleGatewayImpl implements RoleGateway {

  private final RoleDOMapper roleDOMapper;
  private final RoleConverter roleConverter;

  @Override
  public Role save(Role role) {
    RoleDO roleDO = roleConverter.toDataObject(role);
    roleDOMapper.insert(roleDO);
    role = roleConverter.toEntity(roleDO);
    return role;
  }

  @Override
  public Role update(Role role) {
    RoleDO roleDO = roleConverter.toDataObject(role);
    roleDOMapper.update(roleDO);
    role = roleConverter.toEntity(roleDO);
    return role;
  }

  @Override
  public void deleteById(String id) {
    roleDOMapper.deleteById(id);
  }

  @Override
  public Optional<Role> findById(String id) {
    RoleDO roleDO = roleDOMapper.selectOneById(id);
    return Optional.ofNullable(roleDO).map(roleConverter::toEntity);
  }

  @Override
  public Optional<Role> findByRoleCode(String roleCode) {
    QueryWrapper query = QueryWrapper.create().where(ROLE_DO.ROLE_CODE.eq(roleCode));
    RoleDO roleDO = roleDOMapper.selectOneByQuery(query);
    return Optional.ofNullable(roleDO).map(roleConverter::toEntity);
  }

  @Override
  public List<Role> findByUserId(String userId) {
    // TODO: 实现根据用户ID查询角色（需要关联 user_role 表）
    return new ArrayList<>();
  }

  @Override
  public List<Role> findByParentId(String parentId) {
    QueryWrapper query = QueryWrapper.create().where(ROLE_DO.PARENT_ID.eq(parentId));
    List<RoleDO> roleDOs = roleDOMapper.selectListByQuery(query);
    return roleDOs.stream().map(roleConverter::toEntity).collect(Collectors.toList());
  }

  @Override
  public List<Role> findAll() {
    List<RoleDO> roleDOs = roleDOMapper.selectAll();
    return roleDOs.stream().map(roleConverter::toEntity).collect(Collectors.toList());
  }

  @Override
  public List<Role> findAllEnabled() {
    QueryWrapper query = QueryWrapper.create().where(ROLE_DO.STATUS.eq((short) 1));
    List<RoleDO> roleDOs = roleDOMapper.selectListByQuery(query);
    return roleDOs.stream().map(roleConverter::toEntity).collect(Collectors.toList());
  }

  @Override
  public boolean existsByRoleCode(String roleCode) {
    QueryWrapper query = QueryWrapper.create().where(ROLE_DO.ROLE_CODE.eq(roleCode));
    return roleDOMapper.selectCountByQuery(query) > 0;
  }

  @Override
  public void assignRoleToUser(String userId, String roleId, String operatorId) {
    // TODO: 实现分配角色给用户（需要操作 user_role 表）
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void removeRoleFromUser(String userId, String roleId) {
    // TODO: 实现移除用户角色���需要操作 user_role 表）
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public List<String> getUserRoleIds(String userId) {
    // TODO: 实现获取用户角色ID列表（需要查询 user_role 表）
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void assignPermissionsToRole(String roleId, List<String> permissionIds) {
    // TODO: 实现分配权限给角色（需要操作 role_permission 表）
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void removePermissionsFromRole(String roleId, List<String> permissionIds) {
    // TODO: 实现移除角色权限（需要操作 role_permission 表）
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void assignDepartmentsToRole(String roleId, List<String> departmentIds) {
    // TODO: 实现分配部门给角色（需要操作 role_department 表）
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void removeDepartmentsFromRole(String roleId, List<String> departmentIds) {
    // TODO: 实现移除角色部门（需要操作 role_department 表）
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public List<String> findDepartmentIdsByRoleId(String roleId) {
    // TODO: 实现根据角色ID获取部门ID列表（需要查询 role_department 表）
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public org.springframework.data.domain.Page<Role> findAll(Pageable pageable) {
    Page<RoleDO> page =
        roleDOMapper.paginate(
            Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()), QueryWrapper.create());

    List<Role> roles =
        page.getRecords().stream().map(roleConverter::toEntity).collect(Collectors.toList());

    return new PageImpl<>(roles, pageable, page.getTotalRow());
  }

  @Override
  public long countUsersByRoleId(String roleId) {
    // TODO: 实现统计角色的用户数（需要查询 user_role 表）
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
