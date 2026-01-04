package com.github.loadup.modules.upms.adapter.web.controller;

import com.github.loadup.modules.upms.app.command.UserCreateCommand;
import com.github.loadup.modules.upms.app.command.UserPasswordChangeCommand;
import com.github.loadup.modules.upms.app.command.UserUpdateCommand;
import com.github.loadup.modules.upms.app.dto.PageResult;
import com.github.loadup.modules.upms.app.dto.UserDetailDTO;
import com.github.loadup.modules.upms.app.query.UserQuery;
import com.github.loadup.modules.upms.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * User Management Controller
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Tag(name = "用户管理", description = "用户CRUD、密码修改、角色分配等接口")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(summary = "创建用户", description = "创建新用户并分配角色")
  @PostMapping
  public UserDetailDTO createUser(@Valid @RequestBody UserCreateCommand command) {
    return userService.createUser(command);
  }

  @Operation(summary = "更新用户", description = "更新用户信息")
  @PutMapping("/{id}")
  public UserDetailDTO updateUser(
      @PathVariable Long id, @Valid @RequestBody UserUpdateCommand command) {
    command.setId(id);
    return userService.updateUser(command);
  }

  @Operation(summary = "删除用户", description = "软删除用户")
  @DeleteMapping("/{id}")
  public void deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
  }

  @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
  @GetMapping("/{id}")
  public UserDetailDTO getUserById(@PathVariable Long id) {
    return userService.getUserById(id);
  }

  @Operation(summary = "查询用户列表", description = "分页查询用户列表")
  @GetMapping
  public PageResult<UserDetailDTO> queryUsers(UserQuery query) {
    return userService.queryUsers(query);
  }

  @Operation(summary = "修改密码", description = "用户修改自己的密码")
  @PostMapping("/{id}/change-password")
  public void changePassword(
      @PathVariable Long id, @Valid @RequestBody UserPasswordChangeCommand command) {
    command.setUserId(id);
    userService.changePassword(command);
  }

  @Operation(summary = "锁定用户", description = "锁定用户账号")
  @PostMapping("/{id}/lock")
  public void lockUser(@PathVariable Long id) {
    userService.lockUser(id);
  }

  @Operation(summary = "解锁用户", description = "解锁用户账号")
  @PostMapping("/{id}/unlock")
  public void unlockUser(@PathVariable Long id) {
    userService.unlockUser(id);
  }
}
