package io.github.loadup.modules.upms.app.strategy;

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

import io.github.loadup.modules.upms.app.autoconfigure.UpmsSecurityProperties;
import io.github.loadup.modules.upms.client.constant.LoginType;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUser;
import io.github.loadup.modules.upms.client.dto.LoginCredentials;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 账号密码登录策略
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordLoginStrategy implements LoginStrategy {

    private final UserGateway userGateway;
    private final PasswordEncoder passwordEncoder;
    private final UpmsSecurityProperties securityProperties;

    @Override
    public String getLoginType() {
        return LoginType.PASSWORD;
    }

    @Override
    public AuthenticatedUser authenticate(LoginCredentials credentials) {
        // 1. 查询用户
        User user = userGateway
                .findByUsername(credentials.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 2. 验证密码
        if (!passwordEncoder.matches(credentials.getPassword(), user.getPassword())) {
            // 增加失败次数
            handleLoginFailure(user);
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 检查账号状态
        if (!user.isActive()) {
            throw new RuntimeException("账号已被锁定或停用");
        }

        // 4. 检查是否因失败次数过多而锁定
        if (isAccountLocked(user)) {
            throw new RuntimeException("账号已被锁定，请稍后再试");
        }

        // 5. 更新登录信息
        user.updateLastLogin(credentials.getIpAddress());
        user.resetLoginFailCount();
        userGateway.update(user);

        // 6. 构建认证结果
        return AuthenticatedUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .newUser(false)
                .build();
    }

    /**
     * 检查账号是否被锁定
     */
    private boolean isAccountLocked(User user) {
        if (!Boolean.TRUE.equals(user.getAccountNonLocked())) {
            if (user.getLockedTime() != null) {
                LocalDateTime unlockTime = user.getLockedTime()
                        .plusMinutes(securityProperties.getLogin().getLockDuration());
                if (LocalDateTime.now().isBefore(unlockTime)) {
                    return true;
                } else {
                    // 自动解锁
                    user.unlockAccount();
                    userGateway.update(user);
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(User user) {
        if (Boolean.TRUE.equals(securityProperties.getLogin().getEnableFailureTracking())) {
            user.incrementLoginFailCount();

            if (user.getLoginFailCount() >= securityProperties.getLogin().getMaxFailAttempts()) {
                user.lockAccount();
                log.warn("用户 {} 因失败次数过多被锁定", user.getUsername());
            }

            userGateway.update(user);
        }
    }
}
