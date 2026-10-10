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
import io.github.loadup.modules.upms.client.command.UserCreateCommand;
import io.github.loadup.modules.upms.client.command.UserPasswordChangeCommand;
import io.github.loadup.modules.upms.client.command.UserUpdateCommand;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.facade.UserFacade;
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
    private final UserFacade service;

    public UserController(UserFacade service) {
        this.service = service;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<UserDetailDTO> create(@Valid @RequestBody UserCreateCommand command) {
        return SuccessResponse.of(service.createUser(command));
    }

    @PostMapping("/update")
    @Operation(summary = "Update a user")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<UserDetailDTO> update(@Valid @RequestBody UserUpdateCommand command) {
        return SuccessResponse.of(service.updateUser(command));
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
    public SuccessResponse<UserDetailDTO> detail(@Valid @RequestBody IdQuery query) {
        return SuccessResponse.of(service.getUserById(query));
    }

    @PostMapping("/list")
    @Operation(summary = "List users")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<java.util.Collection<UserDetailDTO>> list(@Valid @RequestBody UserQuery query) {
        return SuccessResponse.ofPage(service.queryUsers(query));
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
