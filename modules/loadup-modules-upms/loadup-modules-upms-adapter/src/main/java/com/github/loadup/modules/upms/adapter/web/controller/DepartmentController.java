package com.github.loadup.modules.upms.adapter.web.controller;

import com.github.loadup.modules.upms.app.command.DepartmentCreateCommand;
import com.github.loadup.modules.upms.app.command.DepartmentUpdateCommand;
import com.github.loadup.modules.upms.app.dto.DepartmentDTO;
import com.github.loadup.modules.upms.app.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Department Management Controller
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Tag(name = "部门管理", description = "部门CRUD、树形结构查询等接口")
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

  private final DepartmentService departmentService;

  @Operation(summary = "创建部门", description = "创建新部门")
  @PostMapping
  public DepartmentDTO createDepartment(@Valid @RequestBody DepartmentCreateCommand command) {
    return departmentService.createDepartment(command);
  }

  @Operation(summary = "更新部门", description = "更新部门信息")
  @PutMapping("/{id}")
  public DepartmentDTO updateDepartment(
      @PathVariable Long id, @Valid @RequestBody DepartmentUpdateCommand command) {
    command.setId(id);
    return departmentService.updateDepartment(command);
  }

  @Operation(summary = "删除部门", description = "软删除部门")
  @DeleteMapping("/{id}")
  public void deleteDepartment(@PathVariable Long id) {
    departmentService.deleteDepartment(id);
  }

  @Operation(summary = "获取部门详情", description = "根据ID获取部门详细信息")
  @GetMapping("/{id}")
  public DepartmentDTO getDepartmentById(@PathVariable Long id) {
    return departmentService.getDepartmentById(id);
  }

  @Operation(summary = "获取部门树", description = "获取所有部门的树形结构")
  @GetMapping("/tree")
  public List<DepartmentDTO> getDepartmentTree() {
    return departmentService.getDepartmentTree();
  }

  @Operation(summary = "获取部门子树", description = "获取指定部门及其子部门的树形结构")
  @GetMapping("/{id}/tree")
  public DepartmentDTO getDepartmentTreeById(@PathVariable Long id) {
    return departmentService.getDepartmentTreeById(id);
  }

  @Operation(summary = "移动部门", description = "将部门移动到另一个父部门下")
  @PostMapping("/{deptId}/move")
  public void moveDepartment(@PathVariable Long deptId, @RequestParam Long newParentId) {
    departmentService.moveDepartment(deptId, newParentId);
  }
}
