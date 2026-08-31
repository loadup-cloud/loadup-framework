package io.github.loadup.modules.upms.infrastructure.repository;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.PERMISSION_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.upms.domain.entity.Permission;
import io.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import io.github.loadup.modules.upms.infrastructure.converter.PermissionConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.PermissionDO;
import io.github.loadup.modules.upms.infrastructure.mapper.PermissionDOMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * Permission Repository Implementation using MyBatis-Flex
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public class PermissionGatewayImpl implements PermissionGateway {

    private final PermissionDOMapper permissionDOMapper;
    private final PermissionConverter permissionConverter;

    @Override
    public Permission save(Permission permission) {
        PermissionDO permissionDO = permissionConverter.toDataObject(permission);
        permissionDOMapper.insert(permissionDO);
        permission = permissionConverter.toEntity(permissionDO);
        return permission;
    }

    @Override
    public Permission update(Permission permission) {
        PermissionDO permissionDO = permissionConverter.toDataObject(permission);
        permissionDOMapper.update(permissionDO);
        permission = permissionConverter.toEntity(permissionDO);
        return permission;
    }

    @Override
    public void deleteById(String id) {
        permissionDOMapper.deleteById(id);
    }

    @Override
    public Optional<Permission> findById(String id) {
        PermissionDO permissionDO = permissionDOMapper.selectOneById(id);
        return Optional.ofNullable(permissionDO).map(permissionConverter::toEntity);
    }

    @Override
    public Optional<Permission> findByPermissionCode(String permissionCode) {
        QueryWrapper query = QueryWrapper.create().where(PERMISSION_DO.PERMISSION_CODE.eq(permissionCode));
        PermissionDO permissionDO = permissionDOMapper.selectOneByQuery(query);
        return Optional.ofNullable(permissionDO).map(permissionConverter::toEntity);
    }

    @Override
    public List<Permission> findByUserId(String userId) {
        // TODO: 实现根据用户ID查询权限（需要关联 user_role 和 role_permission 表）
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Permission> findByRoleId(String roleId) {
        // TODO: 实现根据角色ID查询权限（需要关联 role_permission 表）
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Permission> findByParentId(String parentId) {
        QueryWrapper query = QueryWrapper.create().where(PERMISSION_DO.PARENT_ID.eq(parentId));
        List<PermissionDO> permissionDOs = permissionDOMapper.selectListByQuery(query);
        return permissionDOs.stream().map(permissionConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<Permission> findByPermissionType(Short permissionType) {
        QueryWrapper query = QueryWrapper.create().where(PERMISSION_DO.PERMISSION_TYPE.eq(permissionType));
        List<PermissionDO> permissionDOs = permissionDOMapper.selectListByQuery(query);
        return permissionDOs.stream().map(permissionConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<Permission> findAll() {
        List<PermissionDO> permissionDOs = permissionDOMapper.selectAll();
        return permissionDOs.stream().map(permissionConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<Permission> findAllEnabled() {
        QueryWrapper query = QueryWrapper.create().where(PERMISSION_DO.STATUS.eq((short) 1));
        List<PermissionDO> permissionDOs = permissionDOMapper.selectListByQuery(query);
        return permissionDOs.stream().map(permissionConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<Permission> findMenuPermissions() {
        QueryWrapper query = QueryWrapper.create()
                .where(PERMISSION_DO.PERMISSION_TYPE.in((short) 1, (short) 2))
                .and(PERMISSION_DO.STATUS.eq((short) 1))
                .orderBy(PERMISSION_DO.SORT_ORDER.asc());
        List<PermissionDO> permissionDOs = permissionDOMapper.selectListByQuery(query);
        return permissionDOs.stream().map(permissionConverter::toEntity).collect(Collectors.toList());
    }

    @Override
    public boolean existsByPermissionCode(String permissionCode) {
        QueryWrapper query = QueryWrapper.create().where(PERMISSION_DO.PERMISSION_CODE.eq(permissionCode));
        return permissionDOMapper.selectCountByQuery(query) > 0;
    }

    @Override
    public void assignPermissionToRole(String roleId, String permissionId, String operatorId) {
        // TODO: 实现分配权限给角色（需要操作 role_permission 表）
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void removePermissionFromRole(String roleId, String permissionId) {
        // TODO: 实现移除角色权限（需要操作 role_permission 表）
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void batchAssignPermissionsToRole(String roleId, List<String> permissionIds, String operatorId) {
        // TODO: 实现批量分配权限给角色（需要操作 role_permission 表）
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void removeAllPermissionsFromRole(String roleId) {
        // TODO: 实现移除角色所有权限（需要操作 role_permission 表）
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<String> getRolePermissionIds(String roleId) {
        // TODO: 实现获取角色的权限ID列表（需要查询 role_permission 表）
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public PermissionGatewayImpl(PermissionDOMapper permissionDOMapper, PermissionConverter permissionConverter) {
        this.permissionDOMapper = permissionDOMapper;
        this.permissionConverter = permissionConverter;
    }
}
