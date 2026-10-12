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
package io.github.loadup.modules.upms.app.strategy;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.modules.upms.app.autoconfigure.UpmsSecurityProperties;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUserDTO;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.model.User;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Component
public class PasswordLoginStrategy implements LoginStrategy {
    private final Clock clock;

    private final UserGateway userGateway;
    private final PasswordEncoder passwordEncoder;
    private final UpmsSecurityProperties securityProperties;

    @Override
    public String getLoginType() {
        return LoginType.PASSWORD;
    }

    @Override
    public AuthenticatedUserDTO authenticate(LoginCredentials credentials) {

        User user = userGateway
                .findByUsername(credentials.getUsername())
                .orElseThrow(
                        () -> new CommonException(CommonResultCodeEnum.UNAUTHENTICATED, "Invalid login credentials"));

        // Resolve an expired temporary lock before evaluating account activity.
        if (isAccountLocked(user)) {
            throw new CommonException(CommonResultCodeEnum.INVALID_STATE, "Account is locked");
        }
        if (!user.isActive()) {
            throw new CommonException(CommonResultCodeEnum.INVALID_STATE, "Account is locked or disabled");
        }

        if (!passwordEncoder.matches(credentials.getPassword(), user.getPassword())) {

            handleLoginFailure(user);
            throw new CommonException(CommonResultCodeEnum.UNAUTHENTICATED, "Invalid login credentials");
        }

        user.updateLastLogin(
                credentials.getIpAddress(),
                java.time.LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
        user.resetLoginFailCount();
        userGateway.update(user);

        return AuthenticatedUserDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .newUser(false)
                .build();
    }

    private boolean isAccountLocked(User user) {
        if (!Boolean.TRUE.equals(user.getAccountNonLocked())) {
            if (Short.valueOf((short) 2).equals(user.getStatus()) && user.getLockedTime() != null) {
                LocalDateTime unlockTime = user.getLockedTime()
                        .plusMinutes(securityProperties.getLogin().getLockDuration());
                if (LocalDateTime.ofInstant(clock.instant(), clock.getZone())
                        .isBefore(unlockTime)) {
                    return true;
                } else {

                    user.unlockAccount();
                    userGateway.update(user);
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void handleLoginFailure(User user) {
        if (Boolean.TRUE.equals(securityProperties.getLogin().getEnableFailureTracking())) {
            user.incrementLoginFailCount();

            if (user.getLoginFailCount() >= securityProperties.getLogin().getMaxFailAttempts()) {
                user.lockAccount(java.time.LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
                LogUtil.warn(PasswordLoginStrategy.class, "用户 {} 因失败次数过多被锁定", user.getUsername());
            }

            userGateway.update(user);
        }
    }

    public PasswordLoginStrategy(
            UserGateway userGateway,
            PasswordEncoder passwordEncoder,
            UpmsSecurityProperties securityProperties,
            @Qualifier("upmsClock") Clock clock) {
        this.clock = clock;
        this.userGateway = userGateway;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
    }
}
