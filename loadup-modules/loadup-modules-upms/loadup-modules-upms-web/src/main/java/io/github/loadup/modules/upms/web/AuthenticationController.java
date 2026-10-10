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
import io.github.loadup.modules.upms.client.command.UserRegisterCommand;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.facade.AuthenticationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "UPMS Authentication", description = "Public account registration")
public class AuthenticationController {
    private final AuthenticationFacade authenticationService;

    public AuthenticationController(AuthenticationFacade authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/auth/register")
    @Operation(summary = "Register a user account")
    @SecurityRequirements
    public SuccessResponse<UserDetailDTO> register(@RequestBody UserRegisterCommand command) {
        return SuccessResponse.of(authenticationService.register(command));
    }
}
