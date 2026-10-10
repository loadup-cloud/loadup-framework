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
import io.github.loadup.modules.upms.client.command.DepartmentCreateCommand;
import io.github.loadup.modules.upms.client.command.DepartmentDeleteCommand;
import io.github.loadup.modules.upms.client.command.DepartmentMoveCommand;
import io.github.loadup.modules.upms.client.command.DepartmentUpdateCommand;
import io.github.loadup.modules.upms.client.dto.DepartmentDTO;
import io.github.loadup.modules.upms.client.facade.DepartmentFacade;
import io.github.loadup.modules.upms.client.query.DepartmentTreeQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upms/department")
@Tag(name = "UPMS Departments", description = "Department administration; requires ROLE_SUPER_ADMIN")
public class DepartmentController {
    private final DepartmentFacade service;

    public DepartmentController(DepartmentFacade service) {
        this.service = service;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a department")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<DepartmentDTO> create(@Valid @RequestBody DepartmentCreateCommand command) {
        return SuccessResponse.of(service.createDepartment(command));
    }

    @PostMapping("/update")
    @Operation(summary = "Update a department")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<DepartmentDTO> update(@Valid @RequestBody DepartmentUpdateCommand command) {
        return SuccessResponse.of(service.updateDepartment(command));
    }

    @PostMapping("/delete")
    @Operation(summary = "Delete a department")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> delete(@Valid @RequestBody DepartmentDeleteCommand query) {
        service.deleteDepartment(query.id());
        return SuccessResponse.success();
    }

    @PostMapping("/detail")
    @Operation(summary = "Get department details")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<DepartmentDTO> detail(@Valid @RequestBody IdQuery query) {
        return SuccessResponse.of(service.getDepartmentById(query.id()));
    }

    @PostMapping("/tree")
    @Operation(summary = "Get the department tree")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<List<DepartmentDTO>> tree(@Valid @RequestBody DepartmentTreeQuery request) {
        return SuccessResponse.of(service.getDepartmentTree());
    }

    @PostMapping("/move")
    @Operation(summary = "Move a department to a new parent")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> move(@Valid @RequestBody DepartmentMoveCommand request) {
        service.moveDepartment(request.deptId(), request.newParentId());
        return SuccessResponse.success();
    }
}
