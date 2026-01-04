package com.github.loadup.modules.upms.adapter.web.controller;

import com.github.loadup.modules.upms.app.command.RoleCreateCommand;
import com.github.loadup.modules.upms.app.command.RoleUpdateCommand;
import com.github.loadup.modules.upms.app.dto.PageResult;
import com.github.loadup.modules.upms.app.dto.RoleDTO;
import com.github.loadup.modules.upms.app.query.RoleQuery;
import com.github.loadup.modules.upms.app.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Role Management Controller
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Tag(name = "角色管理", description = "角色CRUD、权限分配、用户分配等接口")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

  private final RoleService roleService;

  @Operation(summary = "创建角色", description = "创建新角色并分配权限")
  @PostMapping
  public RoleDTO createRole(@Valid @RequestBody RoleCreateCommand command) {
    return roleService.createRole(command);
  }

  @Operation(summary = "更新角色", description = "更新角色信息")
  @PutMapping("/{id}")
  public RoleDTO updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateCommand command) {
    command.setId(id);
    return roleService.updateRole(command);
  }

  @Operation(summary = "删除角色", description = "软删除角色")
  @DeleteMapping("/{id}")
  public void deleteRole(@PathVariable Long id) {
    roleService.deleteRole(id);
  }

  @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
  @GetMapping("/{id}")
  public RoleDTO getRoleById(@PathVariable Long id) {
    return roleService.getRoleById(id);
  }

  @Operation(summary = "查询角色列表", description = "分页查询角色列表")
  @GetMapping
  public PageResult<RoleDTO> queryRoles(RoleQuery query) {
    return roleService.queryRoles(query);
  }

  @Operation(summary = "获取角色树", description = "获取角色层级树结构")
  @GetMapping("/tree")
  public List<RoleDTO> getRoleTree() {
    return roleService.getRoleTree();
  }

  @Operation(summary = "分配角色给用户", description = "将角色分配给指定用户")
  @PostMapping("/{roleId}/users/{userId}")
  public void assignRoleToUser(@PathVariable Long roleId, @PathVariable Long userId) {
    roleService.assignRoleToUser(roleId, userId);
  }

  @Operation(summary = "从用户移除角色", description = "从用户移除指定角色")
  @DeleteMapping("/{roleId}/users/{userId}")
  public void removeRoleFromUser(@PathVariable Long roleId, @PathVariable Long userId) {
    roleService.removeRoleFromUser(roleId, userId);
  }

  @Operation(summary = "分配权限给角色", description = "批量分配权限给角色")
  @PostMapping("/{roleId}/permissions")
  public void assignPermissionsToRole(
      @PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
    roleService.assignPermissionsToRole(roleId, permissionIds);
  }
}
