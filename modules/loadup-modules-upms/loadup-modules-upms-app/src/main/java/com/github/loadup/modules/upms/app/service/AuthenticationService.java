package com.github.loadup.modules.upms.app.service;

import com.github.loadup.modules.upms.app.command.UserLoginCommand;
import com.github.loadup.modules.upms.app.command.UserRegisterCommand;
import com.github.loadup.modules.upms.app.dto.LoginResultDTO;
import com.github.loadup.modules.upms.app.dto.UserInfoDTO;
import com.github.loadup.modules.upms.domain.entity.LoginLog;
import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.domain.repository.LoginLogRepository;
import com.github.loadup.modules.upms.domain.repository.RoleRepository;
import com.github.loadup.modules.upms.domain.repository.UserRepository;
import com.github.loadup.modules.upms.domain.service.UserPermissionService;
import com.github.loadup.modules.upms.infrastructure.config.SecurityProperties;
import com.github.loadup.modules.upms.infrastructure.security.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
public class AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final LoginLogRepository loginLogRepository;
  private final UserPermissionService permissionService;
  private final JwtTokenProvider tokenProvider;
  private final PasswordEncoder passwordEncoder;
  private final SecurityProperties securityProperties;

  /** User login */
  @Transactional
  public LoginResultDTO login(UserLoginCommand command) {
    // Validate user
    User user =
        userRepository
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
      // Authenticate
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  command.getUsername(), command.getPassword()));

      // Update user login info
      user.updateLastLogin(command.getIpAddress());
      userRepository.update(user);

      // Record login success
      recordLoginSuccess(user, command);

      // Generate tokens
      String accessToken = tokenProvider.generateToken(user.getUsername(), user.getId());
      String refreshToken = tokenProvider.generateRefreshToken(user.getUsername(), user.getId());

      // Build user info
      UserInfoDTO userInfo = buildUserInfo(user);

      return LoginResultDTO.builder()
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

  /** User register */
  @Transactional
  public UserInfoDTO register(UserRegisterCommand command) {
    // Check if username exists
    if (userRepository.existsByUsername(command.getUsername())) {
      throw new RuntimeException("用户名已存在");
    }

    // Check if email exists
    if (command.getEmail() != null && userRepository.existsByEmail(command.getEmail())) {
      throw new RuntimeException("邮箱已被注册");
    }

    // Check if phone exists
    if (command.getPhone() != null && userRepository.existsByPhone(command.getPhone())) {
      throw new RuntimeException("手机号已被注册");
    }

    // Create user
    User user =
        User.builder()
            .username(command.getUsername())
            .password(passwordEncoder.encode(command.getPassword()))
            .nickname(command.getNickname())
            .email(command.getEmail())
            .phone(command.getPhone())
            .deptId(1L) // Default department
            .status((short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .emailVerified(false)
            .phoneVerified(false)
            .deleted(false)
            .createdBy(0L)
            .createdTime(LocalDateTime.now())
            .build();

    user = userRepository.save(user);

    // Assign default role (if exists)
    assignDefaultRole(user.getId());

    return buildUserInfo(user);
  }

  /** Refresh access token */
  public LoginResultDTO refreshToken(String refreshToken) {
    if (!tokenProvider.validateToken(refreshToken)) {
      throw new RuntimeException("Invalid refresh token");
    }

    String username = tokenProvider.getUsernameFromToken(refreshToken);
    Long userId = tokenProvider.getUserIdFromToken(refreshToken);

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!user.isActive()) {
      throw new RuntimeException("User is not active");
    }

    String newAccessToken = tokenProvider.generateToken(username, userId);
    String newRefreshToken = tokenProvider.generateRefreshToken(username, userId);

    UserInfoDTO userInfo = buildUserInfo(user);

    return LoginResultDTO.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .tokenType("Bearer")
        .expiresIn(securityProperties.getJwt().getExpiration())
        .userInfo(userInfo)
        .build();
  }

  /** Build user info DTO */
  private UserInfoDTO buildUserInfo(User user) {
    List<Role> roles = roleRepository.findByUserId(user.getId());
    Set<String> permissions = permissionService.getUserPermissionCodes(user.getId());

    return UserInfoDTO.builder()
        .id(user.getId())
        .username(user.getUsername())
        .nickname(user.getNickname())
        .realName(user.getRealName())
        .email(user.getEmail())
        .phone(user.getPhone())
        .avatarUrl(user.getAvatarUrl())
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
          userRepository.update(user);
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

      userRepository.update(user);
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

    loginLogRepository.save(log);
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

    loginLogRepository.save(log);
  }

  /** Assign default role to new user */
  private void assignDefaultRole(Long userId) {
    // Try to assign "ROLE_USER" if it exists
    roleRepository
        .findByRoleCode("ROLE_USER")
        .ifPresent(role -> roleRepository.assignRoleToUser(userId, role.getId(), 0L));
  }
}
