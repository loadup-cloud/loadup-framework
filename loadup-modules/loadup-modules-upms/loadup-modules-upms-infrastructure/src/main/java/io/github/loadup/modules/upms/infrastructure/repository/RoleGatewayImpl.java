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

import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.ROLE_DEPARTMENT_DO;
import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.ROLE_DO;
import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.ROLE_PERMISSION_DO;
import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.USER_ROLE_DO;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.dto.PageQuery;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.infrastructure.converter.RoleConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.RoleDO;
import io.github.loadup.modules.upms.infrastructure.dataobject.RoleDepartmentDO;
import io.github.loadup.modules.upms.infrastructure.dataobject.RolePermissionDO;
import io.github.loadup.modules.upms.infrastructure.dataobject.UserRoleDO;
import io.github.loadup.modules.upms.infrastructure.mapper.RoleDOMapper;
import io.github.loadup.modules.upms.infrastructure.mapper.RoleDepartmentDOMapper;
import io.github.loadup.modules.upms.infrastructure.mapper.RolePermissionDOMapper;
import io.github.loadup.modules.upms.infrastructure.mapper.UserRoleDOMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * Role Repository Implementation using MyBatis-Flex
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public class RoleGatewayImpl implements RoleGateway {

    private final RoleDOMapper roleDOMapper;
    private final RoleConverter roleConverter;
    private final UserRoleDOMapper userRoleMapper;
    private final RolePermissionDOMapper rolePermissionMapper;
    private final RoleDepartmentDOMapper roleDepartmentMapper;

    @Override
    public Role save(Role role) {
        if (role.getId() == null) role.setId(UUID.randomUUID().toString());
        RoleDO roleDO = roleConverter.toDataObject(role);
        if (roleDO.getId() == null) roleDO.setId(UUID.randomUUID().toString());
        if (roleDO.getCreatedAt() == null) roleDO.setCreatedAt(LocalDateTime.now());
        if (roleDO.getUpdatedAt() == null) roleDO.setUpdatedAt(roleDO.getCreatedAt());
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
        rolePermissionMapper.deleteByQuery(QueryWrapper.create().where(ROLE_PERMISSION_DO.ROLE_ID.eq(id)));
        roleDepartmentMapper.deleteByQuery(QueryWrapper.create().where(ROLE_DEPARTMENT_DO.ROLE_ID.eq(id)));
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
        List<String> roleIds = getUserRoleIds(userId);
        if (roleIds.isEmpty()) return List.of();
        return roleDOMapper.selectListByIds(roleIds).stream()
                .map(roleConverter::toEntity)
                .toList();
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
        if (userRoleMapper.selectCountByQuery(QueryWrapper.create()
                        .where(USER_ROLE_DO.USER_ID.eq(userId))
                        .and(USER_ROLE_DO.ROLE_ID.eq(roleId)))
                > 0) return;
        UserRoleDO relation = new UserRoleDO();
        initialize(relation);
        relation.setUserId(userId);
        relation.setRoleId(roleId);
        relation.setCreatedBy(operatorId);
        userRoleMapper.insert(relation);
    }

    @Override
    public void removeRoleFromUser(String userId, String roleId) {
        userRoleMapper.deleteByQuery(
                QueryWrapper.create().where(USER_ROLE_DO.USER_ID.eq(userId)).and(USER_ROLE_DO.ROLE_ID.eq(roleId)));
    }

    @Override
    public List<String> getUserRoleIds(String userId) {
        return userRoleMapper.selectListByQuery(QueryWrapper.create().where(USER_ROLE_DO.USER_ID.eq(userId))).stream()
                .map(UserRoleDO::getRoleId)
                .toList();
    }

    @Override
    public void assignPermissionsToRole(String roleId, List<String> permissionIds) {
        for (String permissionId : permissionIds) {
            if (rolePermissionMapper.selectCountByQuery(QueryWrapper.create()
                            .where(ROLE_PERMISSION_DO.ROLE_ID.eq(roleId))
                            .and(ROLE_PERMISSION_DO.PERMISSION_ID.eq(permissionId)))
                    > 0) continue;
            RolePermissionDO relation = new RolePermissionDO();
            initialize(relation);
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            rolePermissionMapper.insert(relation);
        }
    }

    @Override
    public void removePermissionsFromRole(String roleId, List<String> permissionIds) {
        if (permissionIds.isEmpty()) return;
        rolePermissionMapper.deleteByQuery(QueryWrapper.create()
                .where(ROLE_PERMISSION_DO.ROLE_ID.eq(roleId))
                .and(ROLE_PERMISSION_DO.PERMISSION_ID.in(permissionIds)));
    }

    @Override
    public void assignDepartmentsToRole(String roleId, List<String> departmentIds) {
        for (String deptId : departmentIds) {
            if (roleDepartmentMapper.selectCountByQuery(QueryWrapper.create()
                            .where(ROLE_DEPARTMENT_DO.ROLE_ID.eq(roleId))
                            .and(ROLE_DEPARTMENT_DO.DEPT_ID.eq(deptId)))
                    > 0) continue;
            RoleDepartmentDO relation = new RoleDepartmentDO();
            initialize(relation);
            relation.setRoleId(roleId);
            relation.setDeptId(deptId);
            roleDepartmentMapper.insert(relation);
        }
    }

    @Override
    public void removeDepartmentsFromRole(String roleId, List<String> departmentIds) {
        if (departmentIds.isEmpty()) return;
        roleDepartmentMapper.deleteByQuery(QueryWrapper.create()
                .where(ROLE_DEPARTMENT_DO.ROLE_ID.eq(roleId))
                .and(ROLE_DEPARTMENT_DO.DEPT_ID.in(departmentIds)));
    }

    @Override
    public List<String> findDepartmentIdsByRoleId(String roleId) {
        return roleDepartmentMapper
                .selectListByQuery(QueryWrapper.create().where(ROLE_DEPARTMENT_DO.ROLE_ID.eq(roleId)))
                .stream()
                .map(RoleDepartmentDO::getDeptId)
                .toList();
    }

    private static void initialize(io.github.loadup.commons.dataobject.BaseDO relation) {
        relation.setId(UUID.randomUUID().toString());
        relation.setCreatedAt(LocalDateTime.now());
        relation.setUpdatedAt(relation.getCreatedAt());
        relation.setDeleted(0);
    }

    @Override
    public PageResult<Role> findAll(PageQuery query) {
        Page<RoleDO> page = roleDOMapper.paginate(Page.of(query.pageNum(), query.pageSize()), QueryWrapper.create());

        List<Role> roles =
                page.getRecords().stream().map(roleConverter::toEntity).collect(Collectors.toList());

        return PageResult.of(roles, page.getTotalRow(), query.pageNum(), query.pageSize());
    }

    @Override
    public long countUsersByRoleId(String roleId) {
        return userRoleMapper.selectCountByQuery(QueryWrapper.create().where(USER_ROLE_DO.ROLE_ID.eq(roleId)));
    }

    public RoleGatewayImpl(
            RoleDOMapper roleDOMapper,
            RoleConverter roleConverter,
            UserRoleDOMapper userRoleMapper,
            RolePermissionDOMapper rolePermissionMapper,
            RoleDepartmentDOMapper roleDepartmentMapper) {
        this.roleDOMapper = roleDOMapper;
        this.roleConverter = roleConverter;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.roleDepartmentMapper = roleDepartmentMapper;
    }
}
