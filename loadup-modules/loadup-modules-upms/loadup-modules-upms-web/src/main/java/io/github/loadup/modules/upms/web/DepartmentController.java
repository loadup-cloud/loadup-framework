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

import io.github.loadup.modules.upms.app.service.DepartmentService;
import io.github.loadup.modules.upms.client.command.DepartmentCreateCommand;
import io.github.loadup.modules.upms.client.command.DepartmentUpdateCommand;
import io.github.loadup.modules.upms.client.dto.DepartmentDTO;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/upms/department")
public class DepartmentController {
    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public DepartmentDTO create(@RequestBody DepartmentCreateCommand command) {
        return service.createDepartment(command);
    }

    @PostMapping("/update")
    public DepartmentDTO update(@RequestBody DepartmentUpdateCommand command) {
        return service.updateDepartment(command);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody String id) {
        service.deleteDepartment(id);
    }

    @PostMapping("/detail")
    public DepartmentDTO detail(@RequestBody String id) {
        return service.getDepartmentById(id);
    }

    @PostMapping("/tree")
    public List<DepartmentDTO> tree() {
        return service.getDepartmentTree();
    }

    @PostMapping("/move")
    public void move(@RequestParam String deptId, @RequestParam String newParentId) {
        service.moveDepartment(deptId, newParentId);
    }
}
