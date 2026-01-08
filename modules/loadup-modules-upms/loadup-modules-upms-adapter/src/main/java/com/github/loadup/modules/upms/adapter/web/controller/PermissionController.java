package com.github.loadup.modules.upms.adapter.web.controller;

import com.github.loadup.modules.upms.adapter.web.request.IdRequest;
import com.github.loadup.modules.upms.adapter.web.request.PermissionTypeRequest;
import com.github.loadup.modules.upms.app.service.PermissionService;
import com.github.loadup.upms.api.command.PermissionCreateCommand;
import com.github.loadup.upms.api.command.PermissionUpdateCommand;
import com.github.loadup.upms.api.dto.PermissionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Permission Management Controller
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Tag(name = "权限管理", description = "权限CRUD、权限树、按类型查询等接口")
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

  private final PermissionService permissionService;

  @Operation(summary = "创建权限", description = "创建新权限")
  @PostMapping("/create")
  public PermissionDTO createPermission(@Valid @RequestBody PermissionCreateCommand command) {
    PermissionDTO result = permissionService.createPermission(command);
    return result;
  }

  @Operation(summary = "更新权限", description = "更新权限信息")
  @PostMapping("/update")
  public PermissionDTO updatePermission(@Valid @RequestBody PermissionUpdateCommand command) {
    PermissionDTO result = permissionService.updatePermission(command);
    return result;
  }

  @Operation(summary = "删除权限", description = "软删除权限")
  @PostMapping("/delete")
  public void deletePermission(@Valid @RequestBody IdRequest request) {
    permissionService.deletePermission(request.getId());
  }

  @Operation(summary = "获取权限详情", description = "根据ID获取权限详细信息")
  @PostMapping("/get")
  public PermissionDTO getPermissionById(@Valid @RequestBody IdRequest request) {
    PermissionDTO result = permissionService.getPermissionById(request.getId());
    return result;
  }

  @Operation(summary = "获取权限树", description = "获取所有权限的树形结构")
  @PostMapping("/tree")
  public List<PermissionDTO> getPermissionTree() {
    return permissionService.getPermissionTree();
  }

  @Operation(summary = "按类型获取权限", description = "按权限类型获取权限列表（1-菜单, 2-按钮, 3-API）")
  @PostMapping("/by-type")
  public List<PermissionDTO> getPermissionsByType(
      @Valid @RequestBody PermissionTypeRequest request) {
    return permissionService.getPermissionsByType(request.getPermissionType());
  }

  @Operation(summary = "获取用户权限", description = "获取指定用户的所有权限")
  @PostMapping("/user-permissions")
  public List<PermissionDTO> getUserPermissions(@Valid @RequestBody IdRequest request) {
    return permissionService.getUserPermissions(request.getId());
  }

  @Operation(summary = "获取用户菜单树", description = "获取用户的可见菜单树")
  @PostMapping("/user-menu-tree")
  public List<PermissionDTO> getUserMenuTree(@Valid @RequestBody IdRequest request) {
    return permissionService.getUserMenuTree(request.getId());
  }
}
