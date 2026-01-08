package com.github.loadup.modules.upms.adapter.web.controller;

import com.github.loadup.commons.result.PageDTO;
import com.github.loadup.modules.upms.adapter.web.request.IdRequest;
import com.github.loadup.modules.upms.app.dto.UserDetailDTO;
import com.github.loadup.modules.upms.app.query.UserQuery;
import com.github.loadup.modules.upms.app.service.UserService;
import com.github.loadup.upms.api.command.UserCreateCommand;
import com.github.loadup.upms.api.command.UserPasswordChangeCommand;
import com.github.loadup.upms.api.command.UserUpdateCommand;
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
  @PostMapping("/create")
  public UserDetailDTO createUser(@Valid @RequestBody UserCreateCommand command) {
    UserDetailDTO result = userService.createUser(command);
    return result;
  }

  @Operation(summary = "更新用户", description = "更新用户信息")
  @PostMapping("/update")
  public UserDetailDTO updateUser(@Valid @RequestBody UserUpdateCommand command) {
    UserDetailDTO result = userService.updateUser(command);
    return result;
  }

  @Operation(summary = "删除用户", description = "软删除用户")
  @PostMapping("/delete")
  public void deleteUser(@Valid @RequestBody IdRequest request) {
    userService.deleteUser(request.getId());
  }

  @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
  @PostMapping("/get")
  public UserDetailDTO getUserById(@Valid @RequestBody IdRequest request) {
    UserDetailDTO result = userService.getUserById(request.getId());
    return result;
  }

  @Operation(summary = "查询用户列表", description = "分页查询用户列表")
  @PostMapping("/query")
  public PageDTO<UserDetailDTO> queryUsers(@Valid @RequestBody UserQuery query) {
    PageDTO<UserDetailDTO> pageDTO = userService.queryUsers(query);
    return pageDTO;
  }

  @Operation(summary = "修改密码", description = "用户修改自己的密码")
  @PostMapping("/change-password")
  public void changePassword(@Valid @RequestBody UserPasswordChangeCommand command) {
    userService.changePassword(command);
  }

  @Operation(summary = "锁定用户", description = "锁定用户账号")
  @PostMapping("/lock")
  public void lockUser(@Valid @RequestBody IdRequest request) {
    userService.lockUser(request.getId());
  }

  @Operation(summary = "解锁用户", description = "解锁用户账号")
  @PostMapping("/unlock")
  public void unlockUser(@Valid @RequestBody IdRequest request) {
    userService.unlockUser(request.getId());
  }
}
