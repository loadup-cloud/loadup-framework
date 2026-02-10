package io.github.loadup.modules.upms.adapter.web.controller;

/*-
 * #%L
 * Loadup Modules UPMS Adapter Layer
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
import io.github.loadup.modules.upms.adapter.web.request.AssignPermissionsRequest;
import io.github.loadup.modules.upms.adapter.web.request.AssignRoleRequest;
import io.github.loadup.modules.upms.adapter.web.request.IdRequest;
import io.github.loadup.modules.upms.app.query.RoleQuery;
import io.github.loadup.modules.upms.app.service.RoleService;
import io.github.loadup.upms.api.command.RoleCreateCommand;
import io.github.loadup.upms.api.command.RoleUpdateCommand;
import io.github.loadup.upms.api.dto.RoleDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Role Management Controller
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Tag(name = "角色管理", description = "角色CRUD、权限分配、用户分配等接口")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "创建角色", description = "创建新角色并分配权限")
    @PostMapping("/create")
    public RoleDTO createRole(@Valid @RequestBody RoleCreateCommand command) {
        RoleDTO result = roleService.createRole(command);
        return result;
    }

    @Operation(summary = "更新角色", description = "更新角色信息")
    @PostMapping("/update")
    public RoleDTO updateRole(@Valid @RequestBody RoleUpdateCommand command) {
        RoleDTO result = roleService.updateRole(command);
        return result;
    }

    @Operation(summary = "删除角色", description = "软删除角色")
    @PostMapping("/delete")
    public void deleteRole(@Valid @RequestBody IdRequest request) {
        roleService.deleteRole(request.getId());
    }

    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    @PostMapping("/get")
    public RoleDTO getRoleById(@Valid @RequestBody IdRequest request) {
        RoleDTO result = roleService.getRoleById(request.getId());
        return result;
    }

    @Operation(summary = "查询角色列表", description = "分页查询角色列表")
    @PostMapping("/query")
    public PageDTO<RoleDTO> queryRoles(@Valid @RequestBody RoleQuery query) {
        PageDTO<RoleDTO> pageDTO = roleService.queryRoles(query);
        return pageDTO;
    }

    @Operation(summary = "获取角色树", description = "获取角色层级树结构")
    @PostMapping("/tree")
    public List<RoleDTO> getRoleTree() {
        return roleService.getRoleTree();
    }

    @Operation(summary = "分配角色给用户", description = "将角色分配给指定用户")
    @PostMapping("/assign-user")
    public void assignRoleToUser(@Valid @RequestBody AssignRoleRequest request) {
        roleService.assignRoleToUser(request.getRoleId(), request.getUserId());
    }

    @Operation(summary = "从用户移除角色", description = "从用户移除指定角色")
    @PostMapping("/remove-user")
    public void removeRoleFromUser(@Valid @RequestBody AssignRoleRequest request) {
        roleService.removeRoleFromUser(request.getRoleId(), request.getUserId());
    }

    @Operation(summary = "分配权限给角色", description = "批量分配权限给角色")
    @PostMapping("/assign-permissions")
    public void assignPermissionsToRole(@Valid @RequestBody AssignPermissionsRequest request) {
        roleService.assignPermissionsToRole(request.getRoleId(), request.getPermissionIds());
    }
}
