package io.github.loadup.modules.upms.app.service;

import io.github.loadup.modules.upms.app.strategy.LoginStrategyManager;
import io.github.loadup.modules.upms.client.command.UserLoginCommand;
import io.github.loadup.modules.upms.client.command.UserRegisterCommand;
import io.github.loadup.modules.upms.client.constant.LoginType;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUser;
import io.github.loadup.modules.upms.client.dto.LoginCredentials;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.service.AuthenticationService;
import io.github.loadup.modules.upms.domain.entity.LoginLog;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.LoginLogGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.service.UserPermissionService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final LoginLogGateway loginLogGateway;
    private final PasswordEncoder passwordEncoder;
    private final LoginStrategyManager loginStrategyManager;
    private final UserPermissionService permissionService;

    /**
     * User login
     */
    @Override
    public AuthenticatedUser login(UserLoginCommand command) {
        try {
            // 1. 构建登录凭证
            LoginCredentials credentials = buildLoginCredentials(command);

            // 2. 选择登录策略（如果未指定，默认为密码登录）
            String loginType =
                    StringUtils.isNotBlank(command.getLoginType()) ? command.getLoginType() : LoginType.PASSWORD;

            // 3. 执行认证
            log.info(
                    "用户 {} 尝试使用 {} 方式登录",
                    credentials.getUsername() != null ? credentials.getUsername() : credentials.getMobile(),
                    loginType);

            AuthenticatedUser authenticatedUser =
                    loginStrategyManager.getStrategy(loginType).authenticate(credentials);

            // 4. 查询完整用户信息
            User user = userGateway
                    .findById(authenticatedUser.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 5. 记录登录成功日志
            recordLoginSuccess(user, command, loginType);

            return authenticatedUser;

        } catch (Exception e) {
            // 记录登录失败
            recordLoginFailure(
                    command,
                    e.getMessage(),
                    StringUtils.isNotBlank(command.getLoginType()) ? command.getLoginType() : LoginType.PASSWORD);
            throw e;
        }
    }

    /**
     * 构建登录凭证
     */
    private LoginCredentials buildLoginCredentials(UserLoginCommand command) {
        return LoginCredentials.builder()
                .loginType(command.getLoginType())
                .username(command.getUsername())
                .password(command.getPassword())
                .mobile(command.getMobile())
                .smsCode(command.getSmsCode())
                .email(command.getEmail())
                .emailCode(command.getEmailCode())
                .provider(command.getProvider())
                .code(command.getCode())
                .state(command.getState())
                .redirectUri(command.getRedirectUri())
                .ipAddress(command.getIpAddress())
                .userAgent(command.getUserAgent())
                .captchaKey(command.getCaptchaKey())
                .captchaCode(command.getCaptchaCode())
                .build();
    }

    /**
     * User register
     */
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
        User user = new User();
        user.setUsername(command.getUsername());
        user.setPassword(passwordEncoder.encode(command.getPassword()));
        user.setNickname(command.getNickname());
        user.setEmail(command.getEmail());
        user.setMobile(command.getMobile());
        user.setDeptId("1");
        user.setStatus((short) 1);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEmailVerified(false);
        user.setMobileVerified(false);
        user.setDeleted(false);
        user.setCreatedBy("0");
        user.setCreatedTime(LocalDateTime.now());

        user = userGateway.save(user);

        // Assign default role (if exists)
        assignDefaultRole(user.getId());

        return buildUserInfo(user);
    }

    /**
     * Build user info DTO
     */
    private UserDetailDTO buildUserInfo(User user) {
        List<Role> roles = roleGateway.findByUserId(user.getId());
        Set<String> permissions = permissionService.getUserPermissionCodes(user.getId());
        UserDetailDTO userDetailDTO = new UserDetailDTO();
        userDetailDTO.setId(user.getId());
        userDetailDTO.setAccount(user.getUsername());
        userDetailDTO.setNickname(user.getNickname());
        userDetailDTO.setRealName(user.getRealName());
        userDetailDTO.setEmail(user.getEmail());
        userDetailDTO.setMobile(user.getMobile());
        userDetailDTO.setAvatar(user.getAvatar());
        userDetailDTO.setDeptId(user.getDeptId());
        userDetailDTO.setRoles(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));
        userDetailDTO.setPermissions(List.copyOf(permissions));
        userDetailDTO.setLastLoginTime(user.getLastLoginTime());
        return userDetailDTO;
    }

    /**
     * Record login success
     */
    private void recordLoginSuccess(User user, UserLoginCommand command, String loginType) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(user.getId());
        loginLog.setUsername(user.getUsername());
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setIpAddress(command.getIpAddress());
        loginLog.setLoginStatus((short) 1);
        loginLog.setLoginMessage("登录成功");
        loginLog.setLoginType(loginType);
        loginLog.setProvider(command.getProvider());

        loginLogGateway.save(loginLog);
    }

    /**
     * Record login failure
     */
    private void recordLoginFailure(UserLoginCommand command, String message, String loginType) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(command.getUsername() != null ? command.getUsername() : command.getMobile());
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setIpAddress(command.getIpAddress());
        loginLog.setLoginStatus((short) 0);
        loginLog.setLoginMessage(message);
        loginLog.setLoginType(loginType);
        loginLog.setProvider(command.getProvider());

        loginLogGateway.save(loginLog);
    }

    /**
     * Assign default role to new user
     */
    private void assignDefaultRole(String userId) {
        // Try to assign "ROLE_USER" if it exists
        roleGateway
                .findByRoleCode("ROLE_USER")
                .ifPresent(role -> roleGateway.assignRoleToUser(userId, role.getId(), "0"));
    }

    public AuthenticationServiceImpl(
            UserGateway userGateway,
            RoleGateway roleGateway,
            LoginLogGateway loginLogGateway,
            PasswordEncoder passwordEncoder,
            LoginStrategyManager loginStrategyManager,
            UserPermissionService permissionService) {
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.loginLogGateway = loginLogGateway;
        this.passwordEncoder = passwordEncoder;
        this.loginStrategyManager = loginStrategyManager;
        this.permissionService = permissionService;
    }
}
