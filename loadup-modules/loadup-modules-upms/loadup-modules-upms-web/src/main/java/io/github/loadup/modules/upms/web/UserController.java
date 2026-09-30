package io.github.loadup.modules.upms.web;

import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.modules.upms.app.service.UserService;
import io.github.loadup.modules.upms.client.command.UserCreateCommand;
import io.github.loadup.modules.upms.client.command.UserPasswordChangeCommand;
import io.github.loadup.modules.upms.client.command.UserUpdateCommand;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.query.UserQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upms/user")
@Tag(name = "UPMS Users", description = "User administration; requires ROLE_SUPER_ADMIN")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public UserDetailDTO create(@Valid @RequestBody UserCreateCommand command) {
        return service.createUser(command);
    }

    @PostMapping("/update")
    @Operation(summary = "Update a user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public UserDetailDTO update(@Valid @RequestBody UserUpdateCommand command) {
        return service.updateUser(command);
    }

    @PostMapping("/delete")
    @Operation(summary = "Delete a user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> delete(@Valid @RequestBody IdQuery query) {
        service.deleteUser(query.id());
        return SuccessResponse.success();
    }

    @PostMapping("/detail")
    @Operation(summary = "Get user details")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public UserDetailDTO detail(@Valid @RequestBody IdQuery query) {
        return service.getUserById(query);
    }

    @PostMapping("/list")
    @Operation(summary = "List users")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PageDTO<UserDetailDTO> list(@Valid @RequestBody UserQuery query) {
        return service.queryUsers(query);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change a user's password")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> changePassword(@Valid @RequestBody UserPasswordChangeCommand command) {
        service.changePassword(command);
        return SuccessResponse.success();
    }

    @PostMapping("/lock")
    @Operation(summary = "Lock a user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> lock(@Valid @RequestBody IdQuery query) {
        service.lockUser(query.id());
        return SuccessResponse.success();
    }

    @PostMapping("/unlock")
    @Operation(summary = "Unlock a user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> unlock(@Valid @RequestBody IdQuery query) {
        service.unlockUser(query.id());
        return SuccessResponse.success();
    }
}
