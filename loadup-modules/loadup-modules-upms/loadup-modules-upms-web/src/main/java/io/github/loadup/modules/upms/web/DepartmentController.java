package io.github.loadup.modules.upms.web;

import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.modules.upms.app.service.DepartmentService;
import io.github.loadup.modules.upms.client.command.DepartmentCreateCommand;
import io.github.loadup.modules.upms.client.command.DepartmentUpdateCommand;
import io.github.loadup.modules.upms.client.dto.DepartmentDTO;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upms/department")
public class DepartmentController {
    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DepartmentDTO create(@RequestBody DepartmentCreateCommand command) {
        return service.createDepartment(command);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DepartmentDTO update(@RequestBody DepartmentUpdateCommand command) {
        return service.updateDepartment(command);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> delete(@RequestBody String id) {
        service.deleteDepartment(id);
        return SuccessResponse.success();
    }

    @PostMapping("/detail")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public DepartmentDTO detail(@RequestBody String id) {
        return service.getDepartmentById(id);
    }

    @PostMapping("/tree")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public List<DepartmentDTO> tree() {
        return service.getDepartmentTree();
    }

    @PostMapping("/move")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Void> move(@RequestParam String deptId, @RequestParam String newParentId) {
        service.moveDepartment(deptId, newParentId);
        return SuccessResponse.success();
    }
}
