package com.github.loadup.modules.upms.adapter.web.controller;

import com.github.loadup.modules.upms.app.command.PermissionCreateCommand;
import com.github.loadup.modules.upms.app.command.PermissionUpdateCommand;
import com.github.loadup.modules.upms.app.dto.PermissionDTO;
import com.github.loadup.modules.upms.app.service.PermissionService;
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
@Tag(name = "权限管理", description = "权限CRUD、树形结构查询等接口")
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

  private final PermissionService permissionService;

  @Operation(summary = "创建权限", description = "创建新权限")
  @PostMapping
  public PermissionDTO createPermission(@Valid @RequestBody PermissionCreateCommand command) {
    return permissionService.createPermission(command);
  }

  @Operation(summary = "更新权限", description = "更新权限信息")
  @PutMapping("/{id}")
  public PermissionDTO updatePermission(
      @PathVariable Long id, @Valid @RequestBody PermissionUpdateCommand command) {
    command.setId(id);
    return permissionService.updatePermission(command);
  }

  @Operation(summary = "删除权限", description = "软删除权限")
  @DeleteMapping("/{id}")
  public void deletePermission(@PathVariable Long id) {
    permissionService.deletePermission(id);
  }

  @Operation(summary = "获取权限详情", description = "根据ID获取权限详细信息")
  @GetMapping("/{id}")
  public PermissionDTO getPermissionById(@PathVariable Long id) {
    return permissionService.getPermissionById(id);
  }

  @Operation(summary = "获取权限树", description = "获取所有权限的树形结构")
  @GetMapping("/tree")
  public List<PermissionDTO> getPermissionTree() {
    return permissionService.getPermissionTree();
  }

  @Operation(summary = "按类型获取权限", description = "根据权限类型获取权限列表 (1-菜单, 2-按钮, 3-API)")
  @GetMapping("/type/{permissionType}")
  public List<PermissionDTO> getPermissionsByType(@PathVariable Short permissionType) {
    return permissionService.getPermissionsByType(permissionType);
  }

  @Operation(summary = "获取用户权限", description = "获取指定用户的所有权限")
  @GetMapping("/user/{userId}")
  public List<PermissionDTO> getUserPermissions(@PathVariable Long userId) {
    return permissionService.getUserPermissions(userId);
  }

  @Operation(summary = "获取用户菜单树", description = "获取用户可见的菜单树")
  @GetMapping("/user/{userId}/menu-tree")
  public List<PermissionDTO> getUserMenuTree(@PathVariable Long userId) {
    return permissionService.getUserMenuTree(userId);
  }
}
