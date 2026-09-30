package io.github.loadup.modules.upms.web;

import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.modules.upms.app.service.RoleService;
import io.github.loadup.modules.upms.client.command.RoleCreateCommand;
import io.github.loadup.modules.upms.client.command.RoleUpdateCommand;
import io.github.loadup.modules.upms.client.dto.RoleDTO;
import io.github.loadup.modules.upms.client.query.RoleQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a role")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public RoleDTO create(@Valid @RequestBody RoleCreateCommand command) {
        return service.createRole(command);
    }

    @PostMapping("/update")
    @Operation(summary = "Update a role")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public RoleDTO update(@Valid @RequestBody RoleUpdateCommand command) {
        return service.updateRole(command);
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
    public RoleDTO detail(@Valid @RequestBody IdQuery query) {
        return service.getRoleById(query.id());
    }

    @PostMapping("/list")
    @Operation(summary = "List roles")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PageDTO<RoleDTO> list(@Valid @RequestBody RoleQuery query) {
        return service.queryRoles(query);
    }

    @PostMapping("/tree")
    @Operation(summary = "Get the role hierarchy")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<RoleDTO> tree(@Valid @RequestBody EmptyRequest request) {
        return service.getRoleTree();
    }

    @PostMapping("/assign-to-user")
    @Operation(summary = "Assign a role to a user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> assignToUser(@Valid @RequestBody RoleUserRequest request) {
        service.assignRoleToUser(request.roleId(), request.userId());
        return SuccessResponse.success();
    }

    @PostMapping("/remove-from-user")
    @Operation(summary = "Remove a role from a user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> removeFromUser(@Valid @RequestBody RoleUserRequest request) {
        service.removeRoleFromUser(request.roleId(), request.userId());
        return SuccessResponse.success();
    }

    @PostMapping("/assign-permissions")
    @Operation(summary = "Replace a role's permissions")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> assignPermissions(@Valid @RequestBody RolePermissionsRequest request) {
        service.assignPermissionsToRole(request.roleId(), request.permissionIds());
        return SuccessResponse.success();
    }

    public record RoleUserRequest(
            @NotBlank @Schema(description = "Role ID") String roleId,
            @NotBlank @Schema(description = "User ID") String userId) {}

    public record RolePermissionsRequest(
            @NotBlank @Schema(description = "Role ID") String roleId,
            @NotNull @Schema(description = "Permission IDs to assign") List<String> permissionIds) {}
}
