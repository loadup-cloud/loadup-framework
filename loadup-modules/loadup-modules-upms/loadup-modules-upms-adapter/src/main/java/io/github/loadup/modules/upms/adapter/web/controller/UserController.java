package io.github.loadup.modules.upms.adapter.web.controller;

/*-
 * #%L
 * Loadup Modules UPMS Adapter Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.upms.adapter.web.request.IdRequest;
import io.github.loadup.modules.upms.app.dto.UserDetailDTO;
import io.github.loadup.modules.upms.app.query.UserQuery;
import io.github.loadup.modules.upms.app.service.UserService;
import io.github.loadup.upms.api.command.UserCreateCommand;
import io.github.loadup.upms.api.command.UserPasswordChangeCommand;
import io.github.loadup.upms.api.command.UserUpdateCommand;
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
