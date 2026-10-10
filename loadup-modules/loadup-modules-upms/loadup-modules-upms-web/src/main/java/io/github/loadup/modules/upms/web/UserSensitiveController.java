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

import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.components.authorization.model.LoadUpUser;
import io.github.loadup.modules.upms.client.dto.UserSensitiveDTO;
import io.github.loadup.modules.upms.client.facade.UserSensitiveReadFacade;
import io.github.loadup.modules.upms.client.query.UserSensitiveQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upms/user")
@Tag(name = "User Sensitive Data")
public class UserSensitiveController {
    private final UserSensitiveReadFacade service;

    public UserSensitiveController(UserSensitiveReadFacade service) {
        this.service = service;
    }

    @PostMapping("/sensitive")
    @PreAuthorize("hasAuthority('upms:user:sensitive:read')")
    @Operation(summary = "Read plaintext personal data with authorization and durable audit")
    public SuccessResponse<UserSensitiveDTO> read(
            @Valid @RequestBody UserSensitiveQuery query, Authentication authentication, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoadUpUser user)) {
            throw new AccessDeniedException("Authenticated user is required");
        }
        return SuccessResponse.of(service.read(user.getUserId(), query));
    }
}
