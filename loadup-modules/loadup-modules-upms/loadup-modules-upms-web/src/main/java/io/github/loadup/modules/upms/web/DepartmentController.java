package io.github.loadup.modules.upms.web;

import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.modules.upms.app.service.DepartmentService;
import io.github.loadup.modules.upms.client.command.DepartmentCreateCommand;
import io.github.loadup.modules.upms.client.command.DepartmentUpdateCommand;
import io.github.loadup.modules.upms.client.dto.DepartmentDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a department")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DepartmentDTO create(@Valid @RequestBody DepartmentCreateCommand command) {
        return service.createDepartment(command);
    }

    @PostMapping("/update")
    @Operation(summary = "Update a department")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DepartmentDTO update(@Valid @RequestBody DepartmentUpdateCommand command) {
        return service.updateDepartment(command);
    }

    @PostMapping("/delete")
    @Operation(summary = "Delete a department")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> delete(@Valid @RequestBody IdQuery query) {
        service.deleteDepartment(query.id());
        return SuccessResponse.success();
    }

    @PostMapping("/detail")
    @Operation(summary = "Get department details")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DepartmentDTO detail(@Valid @RequestBody IdQuery query) {
        return service.getDepartmentById(query.id());
    }

    @PostMapping("/tree")
    @Operation(summary = "Get the department tree")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<DepartmentDTO> tree(@Valid @RequestBody EmptyRequest request) {
        return service.getDepartmentTree();
    }

    @PostMapping("/move")
    @Operation(summary = "Move a department to a new parent")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> move(@Valid @RequestBody MoveRequest request) {
        service.moveDepartment(request.deptId(), request.newParentId());
        return SuccessResponse.success();
    }

    public record MoveRequest(
            @NotBlank @Schema(description = "Department ID") String deptId,
            @NotBlank @Schema(description = "New parent department ID") String newParentId) {}
}
