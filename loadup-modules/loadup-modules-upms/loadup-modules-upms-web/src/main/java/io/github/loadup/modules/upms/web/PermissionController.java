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
import io.github.loadup.components.authorization.context.UserContext;
import io.github.loadup.modules.upms.client.command.PermissionCreateCommand;
import io.github.loadup.modules.upms.client.command.PermissionUpdateCommand;
import io.github.loadup.modules.upms.client.dto.PermissionDTO;
import io.github.loadup.modules.upms.client.facade.PermissionFacade;
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
@RequestMapping("/upms/permission")
@Tag(name = "UPMS Permissions", description = "Permission administration and user menus")
public class PermissionController {
    private final PermissionFacade service;

    public PermissionController(PermissionFacade service) {
        this.service = service;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a permission", description = "Requires ROLE_SUPER_ADMIN.")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<PermissionDTO> create(@Valid @RequestBody PermissionCreateCommand command) {
        return SuccessResponse.of(service.createPermission(command));
    }

    @PostMapping("/update")
    @Operation(summary = "Update a permission", description = "Requires ROLE_SUPER_ADMIN.")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<PermissionDTO> update(@Valid @RequestBody PermissionUpdateCommand command) {
        return SuccessResponse.of(service.updatePermission(command));
    }

    @PostMapping("/delete")
    @Operation(summary = "Delete a permission", description = "Requires ROLE_SUPER_ADMIN.")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> delete(@Valid @RequestBody IdQuery query) {
        service.deletePermission(query.id());
        return SuccessResponse.success();
    }

    @PostMapping("/detail")
    @Operation(summary = "Get permission details", description = "Requires ROLE_SUPER_ADMIN.")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<PermissionDTO> detail(@Valid @RequestBody IdQuery query) {
        return SuccessResponse.of(service.getPermissionById(query.id()));
    }

    @PostMapping("/tree")
    @Operation(summary = "Get the permission tree", description = "Requires ROLE_SUPER_ADMIN.")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<List<PermissionDTO>> tree(@Valid @RequestBody EmptyRequest request) {
        return SuccessResponse.of(service.getPermissionTree());
    }

    @PostMapping("/user-menu")
    @Operation(summary = "Get the current user's menu tree", description = "Requires an authenticated user.")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<List<PermissionDTO>> userMenu(@Valid @RequestBody EmptyRequest request) {
        String userId = UserContext.getUserId();
        if (userId == null) throw new IllegalStateException("Authenticated user is required");
        return SuccessResponse.of(service.getUserMenuTree(userId));
    }
}
