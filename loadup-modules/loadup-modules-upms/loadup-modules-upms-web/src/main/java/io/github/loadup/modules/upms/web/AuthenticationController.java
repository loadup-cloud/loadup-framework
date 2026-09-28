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

import io.github.loadup.modules.upms.client.command.UserLoginCommand;
import io.github.loadup.modules.upms.client.command.UserRegisterCommand;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUser;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.service.AuthenticationService;
import io.github.loadup.modules.upms.client.service.UserQueryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    private final UserQueryService userQueryService;

    public AuthenticationController(AuthenticationService authenticationService, UserQueryService userQueryService) {
        this.authenticationService = authenticationService;
        this.userQueryService = userQueryService;
    }

    @PostMapping("/api/auth/register")
    public UserDetailDTO register(@RequestBody UserRegisterCommand command) {
        return authenticationService.register(command);
    }

    @PostMapping("/api/auth/login")
    public UserDetailDTO login(@RequestBody UserLoginCommand command) {
        AuthenticatedUser login = authenticationService.login(command);
        UserDetailDTO userById = userQueryService.getUserById(Long.valueOf(login.getUserId()));
        return userById;
    }
}
