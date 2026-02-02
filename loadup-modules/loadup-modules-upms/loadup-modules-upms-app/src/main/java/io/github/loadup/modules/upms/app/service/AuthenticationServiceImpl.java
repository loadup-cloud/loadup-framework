package io.github.loadup.modules.upms.app.service;

/*-
 * #%L
 * Loadup Modules UPMS App Layer
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

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.upms.domain.entity.LoginLog;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.LoginLogGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.service.UserPermissionService;
import io.github.loadup.modules.upms.security.config.UpmsSecurityProperties;
import io.github.loadup.modules.upms.security.core.SecurityUser;
import io.github.loadup.modules.upms.security.util.JwtTokenProvider;
import io.github.loadup.upms.api.command.UserLoginCommand;
import io.github.loadup.upms.api.command.UserRegisterCommand;
import io.github.loadup.upms.api.constant.UpmsResultCode;
import io.github.loadup.upms.api.dto.*;
import io.github.loadup.upms.api.dto.AccessTokenDTO;
import io.github.loadup.upms.api.dto.AuthUserDTO;
import io.github.loadup.upms.api.dto.UserDetailDTO;
import io.github.loadup.upms.api.gateway.AuthGateway;
import io.github.loadup.upms.api.service.AuthenticationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service Handles user login, register, and token management
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final UserGateway userGateway;
  private final AuthGateway authGateway;
  private final RoleGateway roleGateway;
  private final LoginLogGateway loginLogGateway;
  private final UserPermissionService permissionService;
  private final JwtTokenProvider tokenProvider;
  private final PasswordEncoder passwordEncoder;
  private final UpmsSecurityProperties securityProperties;

  /** User login */
  @Transactional
  @Override
  public AccessTokenDTO login(UserLoginCommand command) {
    // Validate user
    log.info("用户 {} 尝试登录", command.getUsername());
    User user =
        userGateway
            .findByUsername(command.getUsername())
            .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

    // Check account status
    if (!user.isActive()) {
      recordLoginFailure(user, command, "账号已被锁定或停用");
      throw new RuntimeException("账号已被锁定或停用");
    }

    // Check if account is locked due to failed attempts
    if (isAccountLocked(user)) {
      throw new RuntimeException("账号已被锁定，请稍后再试");
    }

    try {
      // 1. 创建未认证的 Authentication 对象
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken(command.getUsername(), command.getPassword());

      // 2. 调用 AuthenticationManager 执行认证
      // 此处会触发 UserDetailsServiceImpl.loadUserByUsername
      // 并在内部调用 AuthGateway 查库和进行密码比对
      Authentication authentication = authenticationManager.authenticate(authenticationToken);

      // 3. 认证通过，获取 Principal（即我们自定义的 SecurityUser）
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      // 4. 生成 JWT 令牌 (自包含 AuthUserDTO 和 Permissions)
      String accessToken = tokenProvider.createToken(securityUser);
      String refreshToken = tokenProvider.createRefreshToken(securityUser);
      // 5. 记录登录状态：异步更新最后登录时间

      // Update user login info
      user.updateLastLogin(command.getIpAddress());
      userGateway.update(user);

      // Record login success
      recordLoginSuccess(user, command);

      // Build user info
      UserDetailDTO userInfo = buildUserInfo(user);

      return AccessTokenDTO.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .tokenType("Bearer")
          .expiresIn(securityProperties.getJwt().getExpiration())
          .userInfo(userInfo)
          .build();

    } catch (Exception e) {
      // Handle login failure
      handleLoginFailure(user, command);
      throw new RuntimeException("用户名或密码错误");
    }
  }

  @Override
  public void logout() {
    // 对于无状态 JWT 架构，通常由前端销毁 Token
    // 如果需要主动失效，可在此处将当前 Token 加入 Redis 黑名单
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      log.info("用户 {} 退出登录", auth.getName());
      SecurityContextHolder.clearContext();
    }
  }

  /** User register */
  @Transactional
  @Override
  public UserDetailDTO register(UserRegisterCommand command) {
    // Check if username exists
    if (userGateway.existsByUsername(command.getUsername())) {
      throw new RuntimeException("用户名已存在");
    }

    // Check if email exists
    if (command.getEmail() != null && userGateway.existsByEmail(command.getEmail())) {
      throw new RuntimeException("邮箱已被注册");
    }

    // Check if phone exists
    if (command.getMobile() != null && userGateway.existsByMobile(command.getMobile())) {
      throw new RuntimeException("手机号已被注册");
    }

    // Create user
    User user =
        User.builder()
            .username(command.getUsername())
            .password(passwordEncoder.encode(command.getPassword()))
            .nickname(command.getNickname())
            .email(command.getEmail())
            .mobile(command.getMobile())
            .deptId("1") // Default department
            .status((short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .emailVerified(false)
            .mobileVerified(false)
            .deleted(false)
            .createdBy("0")
            .createdTime(LocalDateTime.now())
            .build();

    user = userGateway.save(user);

    // Assign default role (if exists)
    assignDefaultRole(user.getId());

    return buildUserInfo(user);
  }

  /** Refresh access token */
  @Override
  public AccessTokenDTO refreshToken(String refreshToken) {
    if (!tokenProvider.validateToken(refreshToken)) {
      throw new CommonException(UpmsResultCode.UNAUTHORIZED); // 令牌无效
    }
    // 2. 校验是否为刷新令牌类型 (防止拿 AccessToken 当 RefreshToken 用)
    // 假设你在 getClaims 中逻辑已经包含类型判断，或者在此处手动解析
    String userId = tokenProvider.getUserIdFromToken(refreshToken);
    if (null == userId) {
      throw new CommonException(UpmsResultCode.UNAUTHORIZED);
    }
    // 3. 重新加载最新的用户信息和权限
    // 刷新令牌的意义在于：即使用户在登录期间权限变了，刷新后也能拿到最新权限
    AuthUserDTO authUserDTO = authGateway.getAuthUserByUserId(userId);
    if (authUserDTO == null || authUserDTO.getStatus() != 0) {
      throw new CommonException(UpmsResultCode.USER_LOCKED);
    }
    Set<String> permissions = authGateway.getUserPermissionCodes(userId);
    SecurityUser securityUser = new SecurityUser(authUserDTO, permissions);

    User user =
        userGateway
            .findById(userId)
            .orElseThrow(() -> new CommonException(UpmsResultCode.USER_NOT_FOUND));
    if (!user.isActive()) {
      throw new CommonException(UpmsResultCode.USER_LOCKED);
    }

    // 4. 生成新的 Access Token
    String newAccessToken = tokenProvider.createToken(securityUser);

    // 5.  采用滚动刷新策略：同时也更换 Refresh Token，延长用户活跃时间
    String newRefreshToken = tokenProvider.createRefreshToken(securityUser);

    UserDetailDTO userInfo = buildUserInfo(user);

    return AccessTokenDTO.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .tokenType("Bearer")
        .expiresIn(securityProperties.getJwt().getExpiration())
        .userInfo(userInfo)
        .build();
  }

  /** Build user info DTO */
  private UserDetailDTO buildUserInfo(User user) {
    List<Role> roles = roleGateway.findByUserId(user.getId());
    Set<String> permissions = permissionService.getUserPermissionCodes(user.getId());

    return UserDetailDTO.builder()
        .id(user.getId())
        .account(user.getUsername())
        .nickname(user.getNickname())
        .realName(user.getRealName())
        .email(user.getEmail())
        .mobile(user.getMobile())
        .avatar(user.getAvatar())
        .deptId(user.getDeptId())
        .roles(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()))
        .permissions(List.copyOf(permissions))
        .lastLoginTime(user.getLastLoginTime())
        .build();
  }

  /** Check if account is locked */
  private boolean isAccountLocked(User user) {
    if (!Boolean.TRUE.equals(user.getAccountNonLocked())) {
      if (user.getLockedTime() != null) {
        LocalDateTime unlockTime =
            user.getLockedTime().plusMinutes(securityProperties.getLogin().getLockDuration());
        if (LocalDateTime.now().isBefore(unlockTime)) {
          return true;
        } else {
          // Auto unlock
          user.unlockAccount();
          userGateway.update(user);
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /** Handle login failure */
  private void handleLoginFailure(User user, UserLoginCommand command) {
    if (Boolean.TRUE.equals(securityProperties.getLogin().getEnableFailureTracking())) {
      user.incrementLoginFailCount();

      if (user.getLoginFailCount() >= securityProperties.getLogin().getMaxFailAttempts()) {
        user.lockAccount();
      }

      userGateway.update(user);
    }

    recordLoginFailure(user, command, "密码错误");
  }

  /** Record login success */
  private void recordLoginSuccess(User user, UserLoginCommand command) {
    LoginLog log =
        LoginLog.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .loginTime(LocalDateTime.now())
            .ipAddress(command.getIpAddress())
            .loginStatus((short) 1)
            .loginMessage("登录成功")
            .build();

    loginLogGateway.save(log);
  }

  /** Record login failure */
  private void recordLoginFailure(User user, UserLoginCommand command, String message) {
    LoginLog log =
        LoginLog.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .loginTime(LocalDateTime.now())
            .ipAddress(command.getIpAddress())
            .loginStatus((short) 0)
            .loginMessage(message)
            .build();

    loginLogGateway.save(log);
  }

  /** Assign default role to new user */
  private void assignDefaultRole(String userId) {
    // Try to assign "ROLE_USER" if it exists
    roleGateway
        .findByRoleCode("ROLE_USER")
        .ifPresent(role -> roleGateway.assignRoleToUser(userId, role.getId(), "0"));
  }
}
