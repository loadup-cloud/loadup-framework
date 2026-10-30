/*
 * #%L
 * Loadup Modules UPMS App Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.modules.upms.app.service;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.modules.upms.app.converter.UpmsDTOConverter;
import io.github.loadup.modules.upms.app.strategy.LoginCredentials;
import io.github.loadup.modules.upms.app.strategy.LoginStrategyManager;
import io.github.loadup.modules.upms.app.strategy.LoginType;
import io.github.loadup.modules.upms.client.command.UserLoginCommand;
import io.github.loadup.modules.upms.client.command.UserRegisterCommand;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUserDTO;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum;
import io.github.loadup.modules.upms.client.facade.AuthenticationFacade;
import io.github.loadup.modules.upms.client.facade.UserQueryFacade;
import io.github.loadup.modules.upms.client.validation.PasswordPolicy;
import io.github.loadup.modules.upms.domain.gateway.LoginLogGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.model.LoginLog;
import io.github.loadup.modules.upms.domain.model.User;
import java.time.Clock;
import java.time.LocalDateTime;
import org.apache.commons.lang3.StringUtils;
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
public class AuthenticationService implements AuthenticationFacade {
    private final Clock clock;
    private final UpmsDTOConverter dtoConverter;

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final LoginLogGateway loginLogGateway;
    private final PasswordEncoder passwordEncoder;
    private final LoginStrategyManager loginStrategyManager;
    private final UserQueryFacade userQueryService;

    public AuthenticationService(
            UserGateway userGateway,
            RoleGateway roleGateway,
            LoginLogGateway loginLogGateway,
            PasswordEncoder passwordEncoder,
            LoginStrategyManager loginStrategyManager,
            UserQueryFacade userQueryService,
            UpmsDTOConverter dtoConverter,
            Clock clock) {
        this.clock = clock;
        this.dtoConverter = dtoConverter;
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.loginLogGateway = loginLogGateway;
        this.passwordEncoder = passwordEncoder;
        this.loginStrategyManager = loginStrategyManager;
        this.userQueryService = userQueryService;
    }

    /**
     * User login
     */
    @Override
    public AuthenticatedUserDTO login(UserLoginCommand command) {
        try {

            LoginCredentials credentials = buildLoginCredentials(command);

            String loginType =
                    StringUtils.isNotBlank(command.getLoginType()) ? command.getLoginType() : LoginType.PASSWORD;

            LogUtil.info(
                    AuthenticationService.class,
                    "用户 {} 尝试使用 {} 方式登录",
                    credentials.getUsername() != null ? credentials.getUsername() : credentials.getMobile(),
                    loginType);

            AuthenticatedUserDTO authenticatedUser =
                    loginStrategyManager.getStrategy(loginType).authenticate(credentials);

            User user = userGateway
                    .findById(authenticatedUser.getUserId())
                    .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.USER_NOT_FOUND));

            recordLoginSuccess(user, command, loginType);

            return authenticatedUser;

        } catch (Exception e) {

            recordLoginFailure(
                    command,
                    e.getMessage(),
                    StringUtils.isNotBlank(command.getLoginType()) ? command.getLoginType() : LoginType.PASSWORD);
            throw e;
        }
    }

    private LoginCredentials buildLoginCredentials(UserLoginCommand command) {
        return dtoConverter.toCredentials(command);
    }

    /**
     * User register
     */
    @Transactional
    @Override
    public UserDetailDTO register(UserRegisterCommand command) {
        if (command == null) {
            throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        }
        PasswordPolicy.requireValid(command.getPassword());
        // Check if username exists
        if (userGateway.existsByUsername(command.getUsername())) {
            throw new CommonException(UpmsResultCodeEnum.USERNAME_ALREADY_EXISTS);
        }

        // Check if email exists
        if (command.getEmail() != null && userGateway.existsByEmail(command.getEmail())) {
            throw new CommonException(UpmsResultCodeEnum.EMAIL_IS_ALREADY_REGISTERED);
        }

        // Check if phone exists
        if (command.getMobile() != null && userGateway.existsByMobile(command.getMobile())) {
            throw new CommonException(UpmsResultCodeEnum.PHONE_NUMBER_IS_ALREADY_REGISTERED);
        }

        // Create user
        User user = new User();
        user.setUsername(command.getUsername());
        user.setPassword(passwordEncoder.encode(command.getPassword()));
        user.setNickname(command.getNickname());
        user.setEmail(command.getEmail());
        user.setMobile(command.getMobile());
        user.setStatus((short) 1);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEmailVerified(false);
        user.setMobileVerified(false);
        user.setDeleted(false);
        user.setCreatedBy("0");

        user = userGateway.save(user);

        // Assign default role (if exists)
        assignDefaultRole(user.getId());

        return userQueryService.getUserById(user.getId());
    }

    /**
     * Record login success
     */
    private void recordLoginSuccess(User user, UserLoginCommand command, String loginType) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(user.getId());
        loginLog.setUsername(user.getUsername());
        loginLog.setLoginTime(LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
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
        String loginIdentity = command.getUsername() != null ? command.getUsername() : command.getMobile();
        if (loginIdentity == null) {
            loginIdentity = command.getEmail();
        }
        loginLog.setUsername(loginIdentity != null ? loginIdentity : "unknown");
        if (command.getUsername() != null) {
            userGateway.findByUsername(command.getUsername()).ifPresent(user -> loginLog.setUserId(user.getId()));
        }
        loginLog.setLoginTime(LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
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
                .filter(role -> role.isEnabled())
                .ifPresent(role -> roleGateway.assignRoleToUser(userId, role.getId(), "0"));
    }
}
