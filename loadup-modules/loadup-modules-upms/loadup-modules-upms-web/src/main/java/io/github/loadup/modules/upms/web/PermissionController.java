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

import io.github.loadup.modules.upms.app.service.PermissionService;
import io.github.loadup.modules.upms.client.command.PermissionCreateCommand;
import io.github.loadup.modules.upms.client.command.PermissionUpdateCommand;
import io.github.loadup.modules.upms.client.dto.PermissionDTO;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/upms/permission")
public class PermissionController {
    private final PermissionService service;

    public PermissionController(PermissionService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public PermissionDTO create(@RequestBody PermissionCreateCommand command) {
        return service.createPermission(command);
    }

    @PostMapping("/update")
    public PermissionDTO update(@RequestBody PermissionUpdateCommand command) {
        return service.updatePermission(command);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody String id) {
        service.deletePermission(id);
    }

    @PostMapping("/detail")
    public PermissionDTO detail(@RequestBody String id) {
        return service.getPermissionById(id);
    }

    @PostMapping("/tree")
    public List<PermissionDTO> tree() {
        return service.getPermissionTree();
    }

    @PostMapping("/user-menu")
    public List<PermissionDTO> userMenu(@RequestBody String userId) {
        return service.getUserMenuTree(userId);
    }
}
