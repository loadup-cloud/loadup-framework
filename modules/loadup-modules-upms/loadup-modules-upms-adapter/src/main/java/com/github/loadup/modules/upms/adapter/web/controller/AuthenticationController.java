package com.github.loadup.modules.upms.adapter.web.controller;

import com.github.loadup.commons.result.*;
import com.github.loadup.modules.upms.adapter.web.request.*;
import com.github.loadup.modules.upms.app.command.*;
import com.github.loadup.modules.upms.app.dto.LoginResultDTO;
import com.github.loadup.modules.upms.app.dto.UserInfoDTO;
import com.github.loadup.modules.upms.app.service.AuthenticationService;
import com.github.loadup.modules.upms.app.service.PasswordResetService;
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
  public IResponse<LoginResultDTO> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    UserLoginCommand command = new UserLoginCommand();
    command.setUsername(request.getUsername());
    command.setPassword(request.getPassword());
    command.setCaptchaKey(request.getCaptchaKey());
    command.setCaptchaCode(request.getCaptchaCode());
    command.setIpAddress(getClientIp(httpRequest));
    command.setUserAgent(httpRequest.getHeader("User-Agent"));

    LoginResultDTO result = authenticationService.login(command);
    return SuccessResponse.of(result);
  }

  @Operation(summary = "用户注册", description = "注册新用户账号")
  @PostMapping("/register")
  public IResponse<UserInfoDTO> register(@Valid @RequestBody RegisterRequest request) {
    UserRegisterCommand command = new UserRegisterCommand();
    command.setUsername(request.getUsername());
    command.setPassword(request.getPassword());
    command.setNickname(request.getNickname());
    command.setEmail(request.getEmail());
    command.setPhone(request.getPhone());
    command.setCaptchaKey(request.getCaptchaKey());
    command.setCaptchaCode(request.getCaptchaCode());
    command.setSmsCode(request.getSmsCode());

    UserInfoDTO result = authenticationService.register(command);
    return SuccessResponse.of(result);
  }

  @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌")
  @PostMapping("/refresh-token")
  public IResponse<LoginResultDTO> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    LoginResultDTO result = authenticationService.refreshToken(request.getRefreshToken());
    return SuccessResponse.of(result);
  }

  @Operation(summary = "发送邮箱验证码", description = "发送密码重置验证码到邮箱")
  @PostMapping("/send-email-code")
  public IResponse sendEmailVerificationCode(@Valid @RequestBody SendEmailCodeRequest request) {
    passwordResetService.sendEmailVerificationCode(request.getEmail());
    return SuccessResponse.success();
  }

  @Operation(summary = "发送短信验证码", description = "发送密码重置验证码到手机")
  @PostMapping("/send-sms-code")
  public IResponse sendSmsVerificationCode(@Valid @RequestBody SendSmsCodeRequest request) {
    passwordResetService.sendSmsVerificationCode(request.getPhone());
    return SuccessResponse.success();
  }

  @Operation(summary = "重置密码", description = "使用验证码重置密码")
  @PostMapping("/reset-password")
  public Response resetPassword(@Valid @RequestBody UserPasswordResetCommand command) {
    passwordResetService.resetPassword(command);
    return Response.buildSuccess();
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
