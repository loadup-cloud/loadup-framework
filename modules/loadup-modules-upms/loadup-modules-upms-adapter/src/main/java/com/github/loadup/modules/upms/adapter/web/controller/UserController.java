package com.github.loadup.modules.upms.adapter.web.controller;

import com.github.loadup.commons.result.PageResponse;
import com.github.loadup.commons.result.Response;
import com.github.loadup.commons.result.SingleResponse;
import com.github.loadup.modules.upms.adapter.web.request.IdRequest;
import com.github.loadup.modules.upms.app.command.UserCreateCommand;
import com.github.loadup.modules.upms.app.command.UserPasswordChangeCommand;
import com.github.loadup.modules.upms.app.command.UserUpdateCommand;
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
  @PostMapping("/create")
  public SingleResponse<UserDetailDTO> createUser(@Valid @RequestBody UserCreateCommand command) {
    UserDetailDTO result = userService.createUser(command);
    return SingleResponse.of(result);
  }

  @Operation(summary = "更新用户", description = "更新用户信息")
  @PostMapping("/update")
  public SingleResponse<UserDetailDTO> updateUser(@Valid @RequestBody UserUpdateCommand command) {
    UserDetailDTO result = userService.updateUser(command);
    return SingleResponse.of(result);
  }

  @Operation(summary = "删除用户", description = "软删除用户")
  @PostMapping("/delete")
  public Response deleteUser(@Valid @RequestBody IdRequest request) {
    userService.deleteUser(request.getId());
    return Response.buildSuccess();
  }

  @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
  @PostMapping("/get")
  public SingleResponse<UserDetailDTO> getUserById(@Valid @RequestBody IdRequest request) {
    UserDetailDTO result = userService.getUserById(request.getId());
    return SingleResponse.of(result);
  }

  @Operation(summary = "查询用户列表", description = "分页查询用户列表")
  @PostMapping("/query")
  public PageResponse<UserDetailDTO> queryUsers(@Valid @RequestBody UserQuery query) {
    com.github.loadup.modules.upms.app.dto.PageResult<UserDetailDTO> pageResult =
        userService.queryUsers(query);
    return PageResponse.of(
        pageResult.getRecords(),
        pageResult.getTotal(),
        (long) pageResult.getSize(),
        (long) pageResult.getPage());
  }

  @Operation(summary = "修改密码", description = "用户修改自己的密码")
  @PostMapping("/change-password")
  public Response changePassword(@Valid @RequestBody UserPasswordChangeCommand command) {
    userService.changePassword(command);
    return Response.buildSuccess();
  }

  @Operation(summary = "锁定用户", description = "锁定用户账号")
  @PostMapping("/lock")
  public Response lockUser(@Valid @RequestBody IdRequest request) {
    userService.lockUser(request.getId());
    return Response.buildSuccess();
  }

  @Operation(summary = "解锁用户", description = "解锁用户账号")
  @PostMapping("/unlock")
  public Response unlockUser(@Valid @RequestBody IdRequest request) {
    userService.unlockUser(request.getId());
    return Response.buildSuccess();
  }
}
