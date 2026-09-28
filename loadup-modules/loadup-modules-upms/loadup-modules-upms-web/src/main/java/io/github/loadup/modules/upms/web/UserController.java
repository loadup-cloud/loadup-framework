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

import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.app.dto.UserDetailDTO;
import io.github.loadup.modules.upms.app.query.UserQuery;
import io.github.loadup.modules.upms.app.service.UserService;
import io.github.loadup.modules.upms.client.command.UserCreateCommand;
import io.github.loadup.modules.upms.client.command.UserPasswordChangeCommand;
import io.github.loadup.modules.upms.client.command.UserUpdateCommand;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/upms/user")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public UserDetailDTO create(@RequestBody UserCreateCommand command) {
        return service.createUser(command);
    }

    @PostMapping("/update")
    public UserDetailDTO update(@RequestBody UserUpdateCommand command) {
        return service.updateUser(command);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody String id) {
        service.deleteUser(id);
    }

    @PostMapping("/detail")
    @PreAuthorize("hasAuthority('internal')")
    public UserDetailDTO detail(@RequestBody IdQuery query) {
        return service.getUserById(query);
    }

    @PostMapping("/list")
    public PageDTO<UserDetailDTO> list(@RequestBody UserQuery query) {
        return service.queryUsers(query);
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasAuthority('internal')")
    public void changePassword(@RequestBody UserPasswordChangeCommand command) {
        service.changePassword(command);
    }

    @PostMapping("/lock")
    public void lock(@RequestBody String id) {
        service.lockUser(id);
    }

    @PostMapping("/unlock")
    public void unlock(@RequestBody String id) {
        service.unlockUser(id);
    }
}
