package io.github.loadup.modules.upms.web;

import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.components.authorization.context.UserContext;
import io.github.loadup.modules.upms.app.service.PermissionService;
import io.github.loadup.modules.upms.client.command.PermissionCreateCommand;
import io.github.loadup.modules.upms.client.command.PermissionUpdateCommand;
import io.github.loadup.modules.upms.client.dto.PermissionDTO;
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
    private final PermissionService service;

    public PermissionController(PermissionService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a permission", description = "Requires ROLE_SUPER_ADMIN.")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PermissionDTO create(@Valid @RequestBody PermissionCreateCommand command) {
        return service.createPermission(command);
    }

    @PostMapping("/update")
    @Operation(summary = "Update a permission", description = "Requires ROLE_SUPER_ADMIN.")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PermissionDTO update(@Valid @RequestBody PermissionUpdateCommand command) {
        return service.updatePermission(command);
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
    public PermissionDTO detail(@Valid @RequestBody IdQuery query) {
        return service.getPermissionById(query.id());
    }

    @PostMapping("/tree")
    @Operation(summary = "Get the permission tree", description = "Requires ROLE_SUPER_ADMIN.")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<PermissionDTO> tree(@Valid @RequestBody EmptyRequest request) {
        return service.getPermissionTree();
    }

    @PostMapping("/user-menu")
    @Operation(summary = "Get the current user's menu tree", description = "Requires an authenticated user.")
    @PreAuthorize("isAuthenticated()")
    public List<PermissionDTO> userMenu(@Valid @RequestBody EmptyRequest request) {
        String userId = UserContext.getUserId();
        if (userId == null) throw new IllegalStateException("Authenticated user is required");
        return service.getUserMenuTree(userId);
    }
}
