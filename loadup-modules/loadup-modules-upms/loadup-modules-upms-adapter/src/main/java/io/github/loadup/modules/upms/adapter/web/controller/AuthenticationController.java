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

import io.github.loadup.modules.upms.adapter.web.request.*;
import io.github.loadup.modules.upms.app.service.PasswordResetService;
import io.github.loadup.upms.api.command.*;
import io.github.loadup.upms.api.command.UserLoginCommand;
import io.github.loadup.upms.api.command.UserPasswordResetCommand;
import io.github.loadup.upms.api.command.UserRegisterCommand;
import io.github.loadup.upms.api.dto.AccessTokenDTO;
import io.github.loadup.upms.api.dto.UserDetailDTO;
import io.github.loadup.upms.api.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller - Handles user authentication endpoints
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Tag(name = "认证管理", description = "用户登录、注册、token刷新等认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final PasswordResetService passwordResetService;

  @Operation(summary = "用户登录", description = "通过用户名密码登录系统")
  @PostMapping("/login")
  public AccessTokenDTO login(
          @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    UserLoginCommand command = new UserLoginCommand();
    command.setUsername(request.getUsername());
    command.setPassword(request.getPassword());
    command.setCaptchaKey(request.getCaptchaKey());
    command.setCaptchaCode(request.getCaptchaCode());
    command.setIpAddress(getClientIp(httpRequest));
    command.setUserAgent(httpRequest.getHeader("User-Agent"));

    AccessTokenDTO result = authenticationService.login(command);
    return result;
  }

  @Operation(summary = "用户注册", description = "注册新用户账号")
  @PostMapping("/register")
  public UserDetailDTO register(@Valid @RequestBody RegisterRequest request) {
    UserRegisterCommand command = new UserRegisterCommand();
    command.setUsername(request.getUsername());
    command.setPassword(request.getPassword());
    command.setNickname(request.getNickname());
    command.setEmail(request.getEmail());
    command.setMobile(request.getMobile());
    command.setCaptchaKey(request.getCaptchaKey());
    command.setCaptchaCode(request.getCaptchaCode());
    command.setSmsCode(request.getSmsCode());

    UserDetailDTO result = authenticationService.register(command);
    return result;
  }

  @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌")
  @PostMapping("/refresh-token")
  public AccessTokenDTO refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    AccessTokenDTO result = authenticationService.refreshToken(request.getRefreshToken());
    return result;
  }

  @Operation(summary = "发送邮箱验证码", description = "发送密码重置验证码到邮箱")
  @PostMapping("/send-email-code")
  public void sendEmailVerificationCode(@Valid @RequestBody SendEmailCodeRequest request) {
    passwordResetService.sendEmailVerificationCode(request.getEmail());
  }

  @Operation(summary = "发送短信验证码", description = "发送密码重置验证码到手机")
  @PostMapping("/send-sms-code")
  public void sendSmsVerificationCode(@Valid @RequestBody SendSmsCodeRequest request) {
    passwordResetService.sendSmsVerificationCode(request.getMobile());
  }

  @Operation(summary = "重置密码", description = "使用验证码重置密码")
  @PostMapping("/reset-password")
  public void resetPassword(@Valid @RequestBody UserPasswordResetCommand command) {
    passwordResetService.resetPassword(command);
  }

  /** Get client IP address */
  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }
}
