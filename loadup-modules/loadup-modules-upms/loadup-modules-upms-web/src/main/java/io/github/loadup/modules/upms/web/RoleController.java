/*
 * #%L
 * LoadUp UPMS Web Adapter
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
package io.github.loadup.modules.upms.web;

import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.modules.upms.client.command.RoleCreateCommand;
import io.github.loadup.modules.upms.client.command.RolePermissionsCommand;
import io.github.loadup.modules.upms.client.command.RoleUpdateCommand;
import io.github.loadup.modules.upms.client.command.RoleUserCommand;
import io.github.loadup.modules.upms.client.dto.RoleDTO;
import io.github.loadup.modules.upms.client.facade.RoleFacade;
import io.github.loadup.modules.upms.client.query.RoleQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upms/role")
@Tag(name = "UPMS Roles", description = "Role administration; requires ROLE_SUPER_ADMIN")
public class RoleController {
    private final RoleFacade service;

    public RoleController(RoleFacade service) {
        this.service = service;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a role")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<RoleDTO> create(@Valid @RequestBody RoleCreateCommand command) {
        return SuccessResponse.of(service.createRole(command));
    }

    @PostMapping("/update")
    @Operation(summary = "Update a role")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<RoleDTO> update(@Valid @RequestBody RoleUpdateCommand command) {
        return SuccessResponse.of(service.updateRole(command));
    }

    @PostMapping("/delete")
    @Operation(summary = "Delete a role")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> delete(@Valid @RequestBody IdQuery query) {
        service.deleteRole(query.id());
        return SuccessResponse.success();
    }

    @PostMapping("/detail")
    @Operation(summary = "Get role details")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<RoleDTO> detail(@Valid @RequestBody IdQuery query) {
        return SuccessResponse.of(service.getRoleById(query.id()));
    }

    @PostMapping("/list")
    @Operation(summary = "List roles")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<java.util.Collection<RoleDTO>> list(@Valid @RequestBody RoleQuery query) {
        return SuccessResponse.ofPage(service.queryRoles(query));
    }

    @PostMapping("/tree")
    @Operation(summary = "Get the role hierarchy")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<List<RoleDTO>> tree(@Valid @RequestBody EmptyRequest request) {
        return SuccessResponse.of(service.getRoleTree());
    }

    @PostMapping("/assign-to-user")
    @Operation(summary = "Assign a role to a user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> assignToUser(@Valid @RequestBody RoleUserCommand request) {
        service.assignRoleToUser(request.roleId(), request.userId());
        return SuccessResponse.success();
    }

    @PostMapping("/remove-from-user")
    @Operation(summary = "Remove a role from a user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> removeFromUser(@Valid @RequestBody RoleUserCommand request) {
        service.removeRoleFromUser(request.roleId(), request.userId());
        return SuccessResponse.success();
    }

    @PostMapping("/assign-permissions")
    @Operation(summary = "Replace a role's permissions")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> assignPermissions(@Valid @RequestBody RolePermissionsCommand request) {
        service.assignPermissionsToRole(request.roleId(), request.permissionIds());
        return SuccessResponse.success();
    }
}
