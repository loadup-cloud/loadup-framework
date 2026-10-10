/*
 * #%L
 * Loadup Modules UPMS App Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
package io.github.loadup.modules.upms.app.service;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.app.converter.UpmsDTOConverter;
import io.github.loadup.modules.upms.client.command.RoleCreateCommand;
import io.github.loadup.modules.upms.client.command.RoleUpdateCommand;
import io.github.loadup.modules.upms.client.dto.PermissionDTO;
import io.github.loadup.modules.upms.client.dto.RoleDTO;
import io.github.loadup.modules.upms.client.query.RoleQuery;
import io.github.loadup.modules.upms.domain.entity.Permission;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.valueobject.DataScope;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Role Management Service
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Service
public class RoleService implements io.github.loadup.modules.upms.client.facade.RoleFacade {
    private final UpmsDTOConverter dtoConverter;

    private final RoleGateway roleGateway;
    private final PermissionGateway permissionGateway;
    private final DepartmentGateway departmentGateway;
    private final UserGateway userGateway;

    /**
     * Create role
     */
    @Transactional
    public RoleDTO createRole(RoleCreateCommand command) {
        // Validate role code uniqueness
        if (roleGateway.existsByRoleCode(command.getRoleCode())) {
            throw new RuntimeException("角色编码已存在");
        }

        validateParent(null, command.getParentId());
        validateScope(command.getDataScope());

        // Create role entity
        Role role = new Role();
        role.setRoleName(command.getRoleName());
        role.setRoleCode(command.getRoleCode());
        role.setParentId(command.getParentId());
        role.setRoleLevel(resolveRoleLevel(command.getParentId()));
        role.setDataScope(command.getDataScope());
        role.setSortOrder(command.getSortOrder());
        role.setStatus(command.getStatus() != null ? command.getStatus() : (short) 1);
        role.setDeleted(false);
        role.setRemark(command.getRemark());
        role.setCreatedBy(command.getCreatedBy());
        role.setCreatedAt(LocalDateTime.now());

        role = roleGateway.save(role);

        // Assign permissions
        if (command.getPermissionIds() != null && !command.getPermissionIds().isEmpty()) {
            validatePermissions(command.getPermissionIds());
            roleGateway.assignPermissionsToRole(role.getId(), command.getPermissionIds());
        }

        // Assign departments (for custom data scope)
        if (command.getDataScope() == 2
                && command.getDepartmentIds() != null
                && !command.getDepartmentIds().isEmpty()) {
            validateDepartments(command.getDepartmentIds());
            roleGateway.assignDepartmentsToRole(role.getId(), command.getDepartmentIds());
        }

        return convertToDTO(role);
    }

    /**
     * Update role
     */
    @Transactional
    public RoleDTO updateRole(RoleUpdateCommand command) {
        Role role = roleGateway.findById(command.getId()).orElseThrow(() -> new RuntimeException("角色不存在"));
        boolean parentChanged =
                command.getParentId() != null && !command.getParentId().equals(role.getParentId());

        if (command.getParentId() != null) validateParent(command.getId(), command.getParentId());
        if (command.getDataScope() != null) validateScope(command.getDataScope());

        // Update role fields
        if (command.getRoleName() != null) {
            role.setRoleName(command.getRoleName());
        }
        if (command.getParentId() != null) {
            role.setParentId(command.getParentId());
            role.setRoleLevel(resolveRoleLevel(command.getParentId()));
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
        role.setUpdatedAt(LocalDateTime.now());

        role = roleGateway.update(role);
        if (parentChanged) updateDescendantLevels(role, new HashSet<>());

        // Update permissions
        if (command.getPermissionIds() != null) {
            List<Permission> currentPermissions = permissionGateway.findByRoleId(role.getId());
            List<String> currentPermissionIds =
                    currentPermissions.stream().map(Permission::getId).collect(Collectors.toList());
            if (!currentPermissionIds.isEmpty()) {
                roleGateway.removePermissionsFromRole(role.getId(), currentPermissionIds);
            }
            if (!command.getPermissionIds().isEmpty()) {
                validatePermissions(command.getPermissionIds());
                roleGateway.assignPermissionsToRole(role.getId(), command.getPermissionIds());
            }
        }

        if (command.getDataScope() != null || command.getDepartmentIds() != null) {
            if (role.getDataScope() != 2 || command.getDepartmentIds() != null) {
                List<String> currentDeptIds = roleGateway.findDepartmentIdsByRoleId(role.getId());
                roleGateway.removeDepartmentsFromRole(role.getId(), currentDeptIds);
            }
            if (role.getDataScope() == 2 && command.getDepartmentIds() != null) {
                validateDepartments(command.getDepartmentIds());
                roleGateway.assignDepartmentsToRole(role.getId(), command.getDepartmentIds());
            }
        }

        return convertToDTO(role);
    }

    /**
     * Delete role
     */
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

    /**
     * Get role by ID
     */
    public RoleDTO getRoleById(String id) {
        Role role = roleGateway.findById(id).orElseThrow(() -> new RuntimeException("角色不存在"));
        return convertToDTO(role);
    }

    /**
     * Query roles with pagination
     */
    public PageDTO<RoleDTO> queryRoles(RoleQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int size = query.getSize() == null ? 20 : query.getSize();
        if (page < 1 || size < 1 || size > 500) throw new IllegalArgumentException("Invalid page or size");
        Comparator<Role> order =
                switch (query.getSortBy() == null ? "sortOrder" : query.getSortBy()) {
                    case "roleName" -> Comparator.comparing(Role::getRoleName, Comparator.nullsLast(String::compareTo));
                    case "roleCode" -> Comparator.comparing(Role::getRoleCode, Comparator.nullsLast(String::compareTo));
                    case "sortOrder" ->
                        Comparator.comparing(Role::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()));
                    default -> throw new IllegalArgumentException("Unsupported role sort field");
                };
        if ("DESC".equalsIgnoreCase(query.getSortOrder())) order = order.reversed();
        List<Role> matches = roleGateway.findAll().stream()
                .filter(role -> query.getRoleName() == null
                        || role.getRoleName() != null && role.getRoleName().contains(query.getRoleName()))
                .filter(role ->
                        query.getRoleCode() == null || role.getRoleCode().equals(query.getRoleCode()))
                .filter(role ->
                        query.getParentId() == null || query.getParentId().equals(role.getParentId()))
                .filter(role -> query.getStatus() == null || query.getStatus().equals(role.getStatus()))
                .filter(role -> query.isDeleted() == null || query.isDeleted().equals(role.getDeleted()))
                .sorted(order.thenComparing(Role::getId))
                .toList();
        long offset = (long) (page - 1) * size;
        List<RoleDTO> result = offset >= matches.size()
                ? List.of()
                : matches.subList((int) offset, (int) Math.min(matches.size(), offset + size)).stream()
                        .map(this::convertToDTO)
                        .toList();
        return PageDTO.of(result, (long) matches.size(), page, size);
    }

    /**
     * Get role tree (hierarchy)
     */
    public List<RoleDTO> getRoleTree() {
        List<Role> allRoles = roleGateway.findAll();
        Map<String, RoleDTO> byId = allRoles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toMap(RoleDTO::getId, Function.identity()));
        List<RoleDTO> roots = new ArrayList<>();
        for (RoleDTO role : byId.values()) {
            RoleDTO parent = byId.get(role.getParentId());
            if (parent == null) {
                roots.add(role);
            } else {
                if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                parent.getChildren().add(role);
            }
        }
        return roots;
    }

    /**
     * Assign role to user
     */
    @Transactional
    public void assignRoleToUser(String roleId, String userId) {
        Role role = roleGateway.findById(roleId).orElseThrow(() -> new RuntimeException("角色不存在"));
        if (!role.isEnabled()) throw new IllegalArgumentException("Role is disabled");
        userGateway.findById(userId).orElseThrow(() -> new IllegalArgumentException("User does not exist"));
        roleGateway.assignRoleToUser(userId, roleId, "0");
    }

    /**
     * Remove role from user
     */
    @Transactional
    public void removeRoleFromUser(String roleId, String userId) {
        roleGateway.removeRoleFromUser(userId, roleId);
    }

    /**
     * Assign permissions to role
     */
    @Transactional
    public void assignPermissionsToRole(String roleId, List<String> permissionIds) {
        roleGateway.findById(roleId).orElseThrow(() -> new RuntimeException("角色不存在"));

        for (String permissionId : permissionIds) {
            permissionGateway.findById(permissionId).orElseThrow(() -> new RuntimeException("权限不存在: " + permissionId));
        }

        roleGateway.assignPermissionsToRole(roleId, permissionIds);
    }

    /**
     * Convert Role entity to RoleDTO
     */
    private RoleDTO convertToDTO(Role role) {
        var permissions = permissionGateway.findByRoleId(role.getId()).stream()
                .map(dtoConverter::toPermissionSummary)
                .toList();
        var departments = roleGateway.findDepartmentIdsByRoleId(role.getId());
        String parentName = role.getParentId() == null
                ? null
                : roleGateway
                        .findById(role.getParentId())
                        .map(Role::getRoleName)
                        .orElse(null);
        return dtoConverter.toRole(role, parentName, permissions, departments);
    }

    /**
     * Convert Permission to PermissionDTO
     */
    private PermissionDTO convertPermissionToDTO(Permission permission) {
        return dtoConverter.toPermissionSummary(permission);
    }

    public RoleService(
            RoleGateway roleGateway,
            PermissionGateway permissionGateway,
            DepartmentGateway departmentGateway,
            UserGateway userGateway,
            UpmsDTOConverter dtoConverter) {
        this.dtoConverter = dtoConverter;
        this.roleGateway = roleGateway;
        this.permissionGateway = permissionGateway;
        this.departmentGateway = departmentGateway;
        this.userGateway = userGateway;
    }

    private void validatePermissions(List<String> permissionIds) {
        for (String permissionId : permissionIds) {
            Permission permission = permissionGateway
                    .findById(permissionId)
                    .orElseThrow(() -> new IllegalArgumentException("Permission does not exist: " + permissionId));
            if (!permission.isEnabled()) throw new IllegalArgumentException("Permission is disabled: " + permissionId);
        }
    }

    private void validateDepartments(List<String> departmentIds) {
        for (String departmentId : departmentIds) {
            if (departmentGateway
                    .findById(departmentId)
                    .filter(d -> d.isEnabled())
                    .isEmpty()) {
                throw new IllegalArgumentException("Department does not exist or is disabled: " + departmentId);
            }
        }
    }

    private void validateScope(Short scope) {
        if (DataScope.fromCode(scope).isEmpty()) {
            throw new IllegalArgumentException("Unknown data scope: " + scope);
        }
    }

    private void validateParent(String roleId, String parentId) {
        Set<String> visited = new HashSet<>();
        String current = parentId;
        while (current != null) {
            if (current.equals(roleId) || !visited.add(current)) {
                throw new IllegalArgumentException("Role hierarchy contains a cycle");
            }
            String parentRoleId = current;
            Role parent = roleGateway
                    .findById(parentRoleId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent role does not exist: " + parentRoleId));
            current = parent.getParentId();
        }
    }

    private int resolveRoleLevel(String parentId) {
        if (parentId == null) return 1;
        Role parent = roleGateway
                .findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent role does not exist: " + parentId));
        return parent.getRoleLevel() == null ? 2 : parent.getRoleLevel() + 1;
    }

    private void updateDescendantLevels(Role parent, Set<String> visited) {
        if (!visited.add(parent.getId())) return;
        for (Role child : roleGateway.findByParentId(parent.getId())) {
            child.setRoleLevel(parent.getRoleLevel() + 1);
            roleGateway.update(child);
            updateDescendantLevels(child, visited);
        }
    }
}
