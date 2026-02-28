package io.github.loadup.modules.upms.app.service;

/*-
 * #%L
 * Loadup Modules UPMS App Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.app.query.RoleQuery;
import io.github.loadup.modules.upms.client.command.RoleCreateCommand;
import io.github.loadup.modules.upms.client.command.RoleUpdateCommand;
import io.github.loadup.modules.upms.client.dto.PermissionDTO;
import io.github.loadup.modules.upms.client.dto.RoleDTO;
import io.github.loadup.modules.upms.domain.entity.Permission;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Role Management Service
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleGateway roleGateway;
    private final PermissionGateway permissionGateway;

    /** Create role */
    @Transactional
    public RoleDTO createRole(RoleCreateCommand command) {
        // Validate role code uniqueness
        if (roleGateway.existsByRoleCode(command.getRoleCode())) {
            throw new RuntimeException("角色编码已存在");
        }

        // Validate parent role exists
        if (command.getParentId() != null) {
            roleGateway.findById(command.getParentId()).orElseThrow(() -> new RuntimeException("父角色不存在"));
        }

        // Create role entity
        Role role = Role.builder()
                .roleName(command.getRoleName())
                .roleCode(command.getRoleCode())
                .parentId(command.getParentId())
                .dataScope(command.getDataScope() != null ? command.getDataScope() : (short) 1)
                .sortOrder(command.getSortOrder())
                .status(command.getStatus() != null ? command.getStatus() : (short) 1)
                .deleted(false)
                .remark(command.getRemark())
                .createdBy(command.getCreatedBy())
                .createdTime(LocalDateTime.now())
                .build();

        role = roleGateway.save(role);

        // Assign permissions
        if (command.getPermissionIds() != null && !command.getPermissionIds().isEmpty()) {
            roleGateway.assignPermissionsToRole(role.getId(), command.getPermissionIds());
        }

        // Assign departments (for custom data scope)
        if (command.getDataScope() == 2
                && command.getDepartmentIds() != null
                && !command.getDepartmentIds().isEmpty()) {
            roleGateway.assignDepartmentsToRole(role.getId(), command.getDepartmentIds());
        }

        return convertToDTO(role);
    }

    /** Update role */
    @Transactional
    public RoleDTO updateRole(RoleUpdateCommand command) {
        Role role = roleGateway.findById(command.getId()).orElseThrow(() -> new RuntimeException("角色不存在"));

        // Validate parent role (prevent circular reference)
        if (command.getParentId() != null) {
            if (command.getParentId().equals(command.getId())) {
                throw new RuntimeException("父角色不能是自己");
            }
            roleGateway.findById(command.getParentId()).orElseThrow(() -> new RuntimeException("父角色不存在"));
        }

        // Update role fields
        if (command.getRoleName() != null) {
            role.setRoleName(command.getRoleName());
        }
        if (command.getParentId() != null) {
            role.setParentId(command.getParentId());
        }
        if (command.getDataScope() != null) {
            role.setDataScope(command.getDataScope());
        }
        if (command.getSortOrder() != null) {
            role.setSortOrder(command.getSortOrder());
        }
        if (command.getStatus() != null) {
            role.setStatus(command.getStatus());
        }
        if (command.getRemark() != null) {
            role.setRemark(command.getRemark());
        }

        role.setUpdatedBy(command.getUpdatedBy());
        role.setUpdatedTime(LocalDateTime.now());

        role = roleGateway.update(role);

        // Update permissions
        if (command.getPermissionIds() != null) {
            List<Permission> currentPermissions = permissionGateway.findByRoleId(role.getId());
            List<String> currentPermissionIds =
                    currentPermissions.stream().map(Permission::getId).collect(Collectors.toList());
            if (!currentPermissionIds.isEmpty()) {
                roleGateway.removePermissionsFromRole(role.getId(), currentPermissionIds);
            }
            if (!command.getPermissionIds().isEmpty()) {
                roleGateway.assignPermissionsToRole(role.getId(), command.getPermissionIds());
            }
        }

        // Update departments (for custom data scope)
        if (command.getDataScope() != null && command.getDataScope() == 2) {
            if (command.getDepartmentIds() != null) {
                List<String> currentDeptIds = roleGateway.findDepartmentIdsByRoleId(role.getId());
                if (!currentDeptIds.isEmpty()) {
                    roleGateway.removeDepartmentsFromRole(role.getId(), currentDeptIds);
                }
                if (!command.getDepartmentIds().isEmpty()) {
                    roleGateway.assignDepartmentsToRole(role.getId(), command.getDepartmentIds());
                }
            }
        }

        return convertToDTO(role);
    }

    /** Delete role */
    @Transactional
    public void deleteRole(String id) {
        roleGateway.findById(id).orElseThrow(() -> new RuntimeException("角色不存在"));

        // Check if role has child roles
        List<Role> childRoles = roleGateway.findByParentId(id);
        if (!childRoles.isEmpty()) {
            throw new RuntimeException("该角色下存在子角色，无法删除");
        }

        // Check if role is assigned to users
        long userCount = roleGateway.countUsersByRoleId(id);
        if (userCount > 0) {
            throw new RuntimeException("该角色已分配给用户，无法删除");
        }

        roleGateway.deleteById(id);
    }

    /** Get role by ID */
    public RoleDTO getRoleById(String id) {
        Role role = roleGateway.findById(id).orElseThrow(() -> new RuntimeException("角色不存在"));
        return convertToDTO(role);
    }

    /** Query roles with pagination */
    public PageDTO<RoleDTO> queryRoles(RoleQuery query) {
        // Sort sort = Sort.by(Sort.Direction.fromString(query.getSortOrder()), query.getSortBy());
        // Pageable pageable = PageRequest.of(query.getPage() - 1, query.getSize(), sort);

        List<Role> allRoles = roleGateway.findAll();

        // Simple pagination manually since repository might not support it

        List<RoleDTO> dtoList = allRoles.stream().map(this::convertToDTO).collect(Collectors.toList());

        return PageDTO.of(dtoList, (long) allRoles.size(), query.getPage(), query.getSize());
    }

    /** Get role tree (hierarchy) */
    public List<RoleDTO> getRoleTree() {
        List<Role> allRoles = roleGateway.findAll();
        return buildRoleTree(allRoles, null);
    }

    /** Assign role to user */
    @Transactional
    public void assignRoleToUser(String roleId, String userId) {
        roleGateway.findById(roleId).orElseThrow(() -> new RuntimeException("角色不存在"));
        roleGateway.assignRoleToUser(userId, roleId, "0");
    }

    /** Remove role from user */
    @Transactional
    public void removeRoleFromUser(String roleId, String userId) {
        roleGateway.removeRoleFromUser(userId, roleId);
    }

    /** Assign permissions to role */
    @Transactional
    public void assignPermissionsToRole(String roleId, List<String> permissionIds) {
        roleGateway.findById(roleId).orElseThrow(() -> new RuntimeException("角色不存在"));

        for (String permissionId : permissionIds) {
            permissionGateway.findById(permissionId).orElseThrow(() -> new RuntimeException("权限不存在: " + permissionId));
        }

        roleGateway.assignPermissionsToRole(roleId, permissionIds);
    }

    /** Convert Role entity to RoleDTO */
    private RoleDTO convertToDTO(Role role) {
        List<Permission> permissions = permissionGateway.findByRoleId(role.getId());
        List<String> departmentIds = roleGateway.findDepartmentIdsByRoleId(role.getId());

        Role parentRole = null;
        if (role.getParentId() != null) {
            parentRole = roleGateway.findById(role.getParentId()).orElse(null);
        }

        return RoleDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .parentId(role.getParentId())
                .parentRoleName(parentRole != null ? parentRole.getRoleName() : null)
                .roleLevel(role.getRoleLevel())
                .dataScope(role.getDataScope())
                .sortOrder(role.getSortOrder())
                .status(role.getStatus())
                .permissions(
                        permissions.stream().map(this::convertPermissionToDTO).collect(Collectors.toList()))
                .departmentIds(departmentIds)
                .remark(role.getRemark())
                .createdTime(role.getCreatedTime())
                .updatedTime(role.getUpdatedTime())
                .build();
    }

    /** Convert Permission to PermissionDTO */
    private PermissionDTO convertPermissionToDTO(Permission permission) {
        return PermissionDTO.builder()
                .id(permission.getId())
                .permissionName(permission.getPermissionName())
                .permissionCode(permission.getPermissionCode())
                .permissionType(permission.getPermissionType())
                .build();
    }

    /** Build role tree recursively */
    private List<RoleDTO> buildRoleTree(List<Role> allRoles, Long parentId) {
        List<RoleDTO> tree = new ArrayList<>();
        for (Role role : allRoles) {
            if ((parentId == null && role.getParentId() == null)
                    || (parentId != null && parentId.equals(role.getParentId()))) {
                RoleDTO dto = convertToDTO(role);
                tree.add(dto);
            }
        }
        return tree;
    }
}
