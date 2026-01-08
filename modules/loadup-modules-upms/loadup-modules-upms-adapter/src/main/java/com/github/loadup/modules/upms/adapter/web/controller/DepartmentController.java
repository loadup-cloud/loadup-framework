package com.github.loadup.modules.upms.adapter.web.controller;

import com.github.loadup.modules.upms.adapter.web.request.IdRequest;
import com.github.loadup.modules.upms.adapter.web.request.MoveDepartmentRequest;
import com.github.loadup.modules.upms.app.service.DepartmentService;
import com.github.loadup.upms.api.command.DepartmentCreateCommand;
import com.github.loadup.upms.api.command.DepartmentUpdateCommand;
import com.github.loadup.upms.api.dto.DepartmentDTO;
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
@Tag(name = "部门管理", description = "部门CRUD、部门树、部门移动等接口")
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

  private final DepartmentService departmentService;

  @Operation(summary = "创建部门", description = "创建新部门")
  @PostMapping("/create")
  public DepartmentDTO createDepartment(@Valid @RequestBody DepartmentCreateCommand command) {
    DepartmentDTO result = departmentService.createDepartment(command);
    return result;
  }

  @Operation(summary = "更新部门", description = "更新部门信息")
  @PostMapping("/update")
  public DepartmentDTO updateDepartment(@Valid @RequestBody DepartmentUpdateCommand command) {
    DepartmentDTO result = departmentService.updateDepartment(command);
    return result;
  }

  @Operation(summary = "删除部门", description = "软删除部门")
  @PostMapping("/delete")
  public void deleteDepartment(@Valid @RequestBody IdRequest request) {
    departmentService.deleteDepartment(request.getId());
  }

  @Operation(summary = "获取部门详情", description = "根据ID获取部门详细信息")
  @PostMapping("/get")
  public DepartmentDTO getDepartmentById(@Valid @RequestBody IdRequest request) {
    DepartmentDTO result = departmentService.getDepartmentById(request.getId());
    return result;
  }

  @Operation(summary = "获取部门树", description = "获取所有部门的树形结构")
  @PostMapping("/tree")
  public List<DepartmentDTO> getDepartmentTree() {
    List<DepartmentDTO> departmentTree = departmentService.getDepartmentTree();
    return departmentTree;
  }

  @Operation(summary = "获取部门子树", description = "获取指定部门的子树")
  @PostMapping("/sub-tree")
  public DepartmentDTO getDepartmentTreeById(@Valid @RequestBody IdRequest request) {
    DepartmentDTO result = departmentService.getDepartmentTreeById(request.getId());
    return result;
  }

  @Operation(summary = "移动部门", description = "将部门移动到新的父部门下")
  @PostMapping("/move")
  public void moveDepartment(@Valid @RequestBody MoveDepartmentRequest request) {
    departmentService.moveDepartment(request.getDeptId(), request.getNewParentId());
  }
}
