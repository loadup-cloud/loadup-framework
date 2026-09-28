package io.github.loadup.modules.upms.web;

import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.components.authorization.context.UserContext;
import io.github.loadup.modules.upms.app.service.PermissionService;
import io.github.loadup.modules.upms.client.command.PermissionCreateCommand;
import io.github.loadup.modules.upms.client.command.PermissionUpdateCommand;
import io.github.loadup.modules.upms.client.dto.PermissionDTO;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upms/permission")
public class PermissionController {
    private final PermissionService service;

    public PermissionController(PermissionService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PermissionDTO create(@RequestBody PermissionCreateCommand command) {
        return service.createPermission(command);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PermissionDTO update(@RequestBody PermissionUpdateCommand command) {
        return service.updatePermission(command);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> delete(@RequestBody String id) {
        service.deletePermission(id);
        return SuccessResponse.success();
    }

    @PostMapping("/detail")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PermissionDTO detail(@RequestBody String id) {
        return service.getPermissionById(id);
    }

    @PostMapping("/tree")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<PermissionDTO> tree() {
        return service.getPermissionTree();
    }

    @PostMapping("/user-menu")
    @PreAuthorize("isAuthenticated()")
    public List<PermissionDTO> userMenu() {
        String userId = UserContext.getUserId();
        if (userId == null) throw new IllegalStateException("Authenticated user is required");
        return service.getUserMenuTree(userId);
    }
}
