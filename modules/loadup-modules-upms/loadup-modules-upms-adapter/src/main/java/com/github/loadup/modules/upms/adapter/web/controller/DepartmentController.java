package com.github.loadup.modules.upms.adapter.web.controller;

import com.github.loadup.commons.result.MultiResponse;
import com.github.loadup.commons.result.Response;
import com.github.loadup.commons.result.SingleResponse;
import com.github.loadup.modules.upms.adapter.web.request.IdRequest;
import com.github.loadup.modules.upms.adapter.web.request.MoveDepartmentRequest;
import com.github.loadup.modules.upms.app.command.DepartmentCreateCommand;
import com.github.loadup.modules.upms.app.command.DepartmentUpdateCommand;
import com.github.loadup.modules.upms.app.dto.DepartmentDTO;
import com.github.loadup.modules.upms.app.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
  public SingleResponse<DepartmentDTO> createDepartment(
      @Valid @RequestBody DepartmentCreateCommand command) {
    DepartmentDTO result = departmentService.createDepartment(command);
    return SingleResponse.of(result);
  }

  @Operation(summary = "更新部门", description = "更新部门信息")
  @PostMapping("/update")
  public SingleResponse<DepartmentDTO> updateDepartment(
      @Valid @RequestBody DepartmentUpdateCommand command) {
    DepartmentDTO result = departmentService.updateDepartment(command);
    return SingleResponse.of(result);
  }

  @Operation(summary = "删除部门", description = "软删除部门")
  @PostMapping("/delete")
  public Response deleteDepartment(@Valid @RequestBody IdRequest request) {
    departmentService.deleteDepartment(request.getId());
    return Response.buildSuccess();
  }

  @Operation(summary = "获取部门详情", description = "根据ID获取部门详细信息")
  @PostMapping("/get")
  public SingleResponse<DepartmentDTO> getDepartmentById(@Valid @RequestBody IdRequest request) {
    DepartmentDTO result = departmentService.getDepartmentById(request.getId());
    return SingleResponse.of(result);
  }

  @Operation(summary = "获取部门树", description = "获取所有部门的树形结构")
  @PostMapping("/tree")
  public MultiResponse<DepartmentDTO> getDepartmentTree() {
    return MultiResponse.of(departmentService.getDepartmentTree());
  }

  @Operation(summary = "获取部门子树", description = "获取指定部门的子树")
  @PostMapping("/sub-tree")
  public SingleResponse<DepartmentDTO> getDepartmentTreeById(
      @Valid @RequestBody IdRequest request) {
    DepartmentDTO result = departmentService.getDepartmentTreeById(request.getId());
    return SingleResponse.of(result);
  }

  @Operation(summary = "移动部门", description = "将部门移动到新的父部门下")
  @PostMapping("/move")
  public Response moveDepartment(@Valid @RequestBody MoveDepartmentRequest request) {
    departmentService.moveDepartment(request.getDeptId(), request.getNewParentId());
    return Response.buildSuccess();
  }
}
