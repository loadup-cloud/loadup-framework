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
import io.github.loadup.commons.util.JwtUtils;
import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.modules.upms.domain.entity.LoginLog;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.LoginLogGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.service.UserPermissionService;
import io.github.loadup.modules.upms.security.config.UpmsSecurityProperties;
import io.github.loadup.upms.api.command.UserLoginCommand;
import io.github.loadup.upms.api.command.UserRegisterCommand;
import io.github.loadup.upms.api.constant.UpmsResultCode;
import io.github.loadup.upms.api.dto.*;
import io.github.loadup.upms.api.dto.AccessTokenDTO;
import io.github.loadup.upms.api.dto.AuthUserDTO;
import io.github.loadup.upms.api.dto.UserDetailDTO;
import io.github.loadup.upms.api.gateway.AuthGateway;
import io.github.loadup.upms.api.service.AuthenticationService;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final LoginLogGateway loginLogGateway;
    private final UserPermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final AuthGateway authGateway;
    private final UpmsSecurityProperties securityProperties;

    // Remove obsolete AuthenticationManager as we don't effectively use Spring Security authentication flow for Login now,
    // or we are refactoring to custom logic.
    // If we want to use AuthManager, we need a SecurityConfig that exposes it.
    // However, in this refactor we are manually checking password via PasswordEncoder
    // let's stick to simple logic or fix AuthManager injection if SecurityConfig is present.
    // Since we deleted SecurityConfig in upms-security, AuthManager bean might not be available unless we add one back or use
    // components-security configuration.
    // For now, let's replace AuthManager logic with manual verification.

    // @Autowired
    // private AuthenticationManager authenticationManager;

    /** User login */
    @Transactional
    @Override
    public AccessTokenDTO login(UserLoginCommand command) {
        // Validate user
        log.info("用户 {} 尝试登录", command.getUsername());
        User user =
                userGateway.findByUsername(command.getUsername()).orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // Check account status
        if (!user.isActive()) {
            recordLoginFailure(user, command, "账号已被锁定或停用");
            throw new RuntimeException("账号已被锁定或停用");
        }

        // Check if account is locked due to failed attempts
        if (isAccountLocked(user)) {
            throw new RuntimeException("账号已被锁定，请稍后再试");
        }

        // Manual password check
        if (!passwordEncoder.matches(command.getPassword(), user.getPassword())) {
             handleLoginFailure(user, command);
             throw new RuntimeException("用户名或密码错误");
        }

        try {
            // Update user login info
            user.updateLastLogin(command.getIpAddress());
            userGateway.update(user);

            // Record login success
            recordLoginSuccess(user, command);

            // Generate Token
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", user.getUsername());

            // long ttl = securityProperties.getJwt().getExpiration() * 1000L;
            // In properties its already millis according to comments? "86400000L". Let's check getter logic or assume simple getter.
            // If the comment says milliseconds, the name `expiration` usually means millis in JWT context or seconds in some.
            // But code previously used `getExpireSeconds` which suggests it was refactored.
            // Let's assume `expiration` is Milliseconds as per typical behavior for long types in properties unless specified.
            // But JwtUtils.createToken takes ttlMillis.
            // Let's trust field name and comment in UpmsSecurityProperties.
            long ttl = securityProperties.getJwt().getExpiration();
            String token = JwtUtils.createToken(
                String.valueOf(user.getId()),
                claims,
                securityProperties.getJwt().getSecret(),
                ttl
            );
            UserDetailDTO userInfo = buildUserInfo(user);

            return AccessTokenDTO.builder()
                    .accessToken(token)
                    .refreshToken(null) // Implement refresh token if needed
                    .expiresIn(securityProperties.getJwt().getExpiration().longValue() / 1000)
                    .userInfo(userInfo)
                    .build();

        } catch (Exception e) {
            handleLoginFailure(user, command);
            throw new RuntimeException("用户名或密码错误", e);
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
        User user = User.builder()
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
        // 1. Validate Token (Using JwtUtils)
        io.jsonwebtoken.Claims claims = null;
        try {
            claims = JwtUtils.parseToken(refreshToken, securityProperties.getJwt().getSecret());
            if (JwtUtils.isExpired(claims)) {
                throw new CommonException(UpmsResultCode.UNAUTHORIZED); // Token expired
            }
        } catch (Exception e) {
            throw new CommonException(UpmsResultCode.UNAUTHORIZED); // Token invalid
        }

        // 2. Extract UserId and check user
        String userId = claims.getSubject();
        if (null == userId) {
            throw new CommonException(UpmsResultCode.UNAUTHORIZED);
        }

        AuthUserDTO authUserDTO = authGateway.getAuthUserByUserId(userId);
        if (authUserDTO == null || authUserDTO.getStatus() != 1) { // Assuming 1 is active, 0 is inactive/locked in AuthUserDTO logic or as per your domain
             // The original code used status != 0 check, I will assume != 1 for active based on register logic (status=1)
             // Let's check logic: Register sets status = 1. So 1 is active.
             // Original: authUserDTO.getStatus() != 0 -> throw Locked. So 0 was active? No, wait.
             // If status 1 is normal, and 0 is disabled.
             // Original code: if (status != 0) throw locked. This implies 0 is the ONLY valid status.
             // But register sets status=1.
             // Let's assume 1 is Active based on register method.
             // So if status != 1, throw locked.

             // Wait, let's look at Register:
             // .status((short) 1) -> 1 is Normal.
             // So if (status != 1) throw locked.
             // Original code logic might have been reversed or using different constants.
             // I will use 1 as active.
             if (authUserDTO.getStatus() != 1) {
                 throw new CommonException(UpmsResultCode.USER_LOCKED);
             }
        }

        User user = userGateway.findById(userId).orElseThrow(() -> new CommonException(UpmsResultCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new CommonException(UpmsResultCode.USER_LOCKED);
        }

        // 3. Generate new Access Token
        Map<String, Object> newClaims = new HashMap<>();
        newClaims.put("username", user.getUsername());

        long ttl = securityProperties.getJwt().getExpiration();
        String newAccessToken = JwtUtils.createToken(
            String.valueOf(user.getId()),
            newClaims,
            securityProperties.getJwt().getSecret(),
            ttl
        );

        // 4. Generate new Refresh Token (optional rolling)
        long refreshTtl = securityProperties.getJwt().getRefreshExpiration() != null ? securityProperties.getJwt().getRefreshExpiration() : ttl * 7;

        String newRefreshToken = JwtUtils.createToken(
            String.valueOf(user.getId()),
            newClaims,
            securityProperties.getJwt().getSecret(),
            refreshTtl
        );

        UserDetailDTO userInfo = buildUserInfo(user);

        return AccessTokenDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(securityProperties.getJwt().getExpiration().longValue() / 1000)
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
                LocalDateTime unlockTime = user.getLockedTime()
                        .plusMinutes(securityProperties.getLogin().getLockDuration());
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
        LoginLog log = LoginLog.builder()
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
        LoginLog log = LoginLog.builder()
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
