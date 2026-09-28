/*-
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

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.modules.upms.app.service.RoleService;
import io.github.loadup.modules.upms.client.command.RoleCreateCommand;
import io.github.loadup.modules.upms.client.command.RoleUpdateCommand;
import io.github.loadup.modules.upms.client.dto.RoleDTO;
import io.github.loadup.modules.upms.client.query.RoleQuery;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upms/role")
public class RoleController {
    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public RoleDTO create(@RequestBody RoleCreateCommand command) {
        return service.createRole(command);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public RoleDTO update(@RequestBody RoleUpdateCommand command) {
        return service.updateRole(command);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> delete(@RequestBody String id) {
        service.deleteRole(id);
        return SuccessResponse.success();
    }

    @PostMapping("/detail")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public RoleDTO detail(@RequestBody String id) {
        return service.getRoleById(id);
    }

    @PostMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PageDTO<RoleDTO> list(@RequestBody RoleQuery query) {
        return service.queryRoles(query);
    }

    @PostMapping("/tree")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<RoleDTO> tree() {
        return service.getRoleTree();
    }

    @PostMapping("/assign-to-user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> assignToUser(@RequestParam String roleId, @RequestParam String userId) {
        service.assignRoleToUser(roleId, userId);
        return SuccessResponse.success();
    }

    @PostMapping("/remove-from-user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> removeFromUser(@RequestParam String roleId, @RequestParam String userId) {
        service.removeRoleFromUser(roleId, userId);
        return SuccessResponse.success();
    }

    @PostMapping("/assign-permissions")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> assignPermissions(@RequestParam String roleId, @RequestBody List<String> permissionIds) {
        service.assignPermissionsToRole(roleId, permissionIds);
        return SuccessResponse.success();
    }
}
