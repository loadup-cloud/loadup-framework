package io.github.loadup.modules.upms.infrastructure.repository;

import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.PERMISSION_DO;
import static io.github.loadup.modules.upms.infrastructure.dataobject.table.Tables.ROLE_PERMISSION_DO;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.upms.domain.entity.Permission;
import io.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import io.github.loadup.modules.upms.infrastructure.converter.PermissionConverter;
import io.github.loadup.modules.upms.infrastructure.dataobject.PermissionDO;
import io.github.loadup.modules.upms.infrastructure.dataobject.RolePermissionDO;
import io.github.loadup.modules.upms.infrastructure.mapper.PermissionDOMapper;
import io.github.loadup.modules.upms.infrastructure.mapper.RolePermissionDOMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    private final RolePermissionDOMapper rolePermissionMapper;

    @Override
    public Permission save(Permission permission) {
        if (permission.getId() == null) permission.setId(UUID.randomUUID().toString());
        PermissionDO permissionDO = permissionConverter.toDataObject(permission);
        if (permissionDO.getId() == null) permissionDO.setId(UUID.randomUUID().toString());
        if (permissionDO.getCreatedAt() == null) permissionDO.setCreatedAt(LocalDateTime.now());
        if (permissionDO.getUpdatedAt() == null) permissionDO.setUpdatedAt(permissionDO.getCreatedAt());
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
        rolePermissionMapper.deleteByQuery(QueryWrapper.create().where(ROLE_PERMISSION_DO.PERMISSION_ID.eq(id)));
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
    public List<Permission> findByRoleId(String roleId) {
        List<String> permissionIds =
                rolePermissionMapper
                        .selectListByQuery(QueryWrapper.create().where(ROLE_PERMISSION_DO.ROLE_ID.eq(roleId)))
                        .stream()
                        .map(RolePermissionDO::getPermissionId)
                        .toList();
        if (permissionIds.isEmpty()) return List.of();
        return permissionDOMapper.selectListByIds(permissionIds).stream()
                .map(permissionConverter::toEntity)
                .toList();
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

    public PermissionGatewayImpl(
            PermissionDOMapper permissionDOMapper,
            PermissionConverter permissionConverter,
            RolePermissionDOMapper rolePermissionMapper) {
        this.permissionDOMapper = permissionDOMapper;
        this.permissionConverter = permissionConverter;
        this.rolePermissionMapper = rolePermissionMapper;
    }
}
