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

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.components.authorization.model.LoadUpUser;
import io.github.loadup.modules.upms.client.command.AccountPasswordChangeCommand;
import io.github.loadup.modules.upms.client.dto.LoginEntryDTO;
import io.github.loadup.modules.upms.client.dto.SecurityOverviewDTO;
import io.github.loadup.modules.upms.client.facade.AccountSecurityFacade;
import io.github.loadup.modules.upms.client.query.AccountLoginQuery;
import io.github.loadup.modules.upms.client.query.AccountSecurityQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Current user's security view; user ID is never accepted from the request. */
@RestController
@RequestMapping("/account/security")
@Tag(name = "Account Security", description = "Self-service password and login history")
public class AccountSecurityController {
    private final AccountSecurityFacade service;

    public AccountSecurityController(AccountSecurityFacade service) {
        this.service = service;
    }

    @PostMapping("/overview")
    @Operation(summary = "Get my account security status")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<SecurityOverviewDTO> overview(
            @RequestBody AccountSecurityQuery request, Authentication authentication) {
        return SuccessResponse.of(service.overview(actor(authentication)));
    }

    @PostMapping("/logins")
    @Operation(summary = "Get my recent login attempts")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<java.util.Collection<LoginEntryDTO>> logins(
            @RequestBody AccountLoginQuery request, Authentication authentication) {
        var result = service.loginHistory(
                actor(authentication),
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size());
        return SuccessResponse.ofPage(PageDTO.of(result.records(), result.total(), result.page(), result.size()));
    }

    @PostMapping("/password")
    @Operation(summary = "Change my password using the current password")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<Void> changePassword(
            @RequestBody AccountPasswordChangeCommand request, Authentication authentication) {
        if (request == null) throw new IllegalArgumentException("password change request is required");
        service.changePassword(
                actor(authentication), request.oldPassword(), request.newPassword(), request.confirmPassword());
        return SuccessResponse.success();
    }

    private static String actor(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoadUpUser user)
                || user.getUserId() == null
                || user.getUserId().isBlank()) {
            throw new IllegalArgumentException("authenticated user is required");
        }
        return user.getUserId();
    }
}
