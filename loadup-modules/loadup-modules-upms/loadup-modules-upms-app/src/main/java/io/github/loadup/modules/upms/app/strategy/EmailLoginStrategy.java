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

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.upms.app.service.VerificationCodeService;
import io.github.loadup.modules.upms.client.dto.AuthenticatedUserDTO;
import io.github.loadup.modules.upms.client.error.UpmsResultCodeEnum;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.model.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Component
public class EmailLoginStrategy implements LoginStrategy {
    private final java.time.Clock clock;

    private final UserGateway userGateway;
    private final VerificationCodeService verificationCodeService;

    @Override
    public String getLoginType() {
        return LoginType.EMAIL;
    }

    @Override
    public AuthenticatedUserDTO authenticate(LoginCredentials credentials) {

        if (StringUtils.isBlank(credentials.getEmail())) {
            throw new CommonException(UpmsResultCodeEnum.EMAIL_IS_REQUIRED);
        }
        if (StringUtils.isBlank(credentials.getEmailCode())) {
            throw new CommonException(UpmsResultCodeEnum.VERIFICATION_CODE_IS_REQUIRED);
        }

        boolean valid = verificationCodeService.verifyEmailCode(credentials.getEmail(), credentials.getEmailCode());
        if (!valid) {
            throw new CommonException(UpmsResultCodeEnum.VERIFICATION_CODE_IS_INVALID_OR_EXPIRED);
        }

        User user = userGateway
                .findByEmail(credentials.getEmail())
                .orElseThrow(() -> new CommonException(UpmsResultCodeEnum.INVALID_LOGIN_CREDENTIALS));

        if (!user.isActive()) {
            throw new CommonException(UpmsResultCodeEnum.ACCOUNT_IS_LOCKED_OR_DISABLED);
        }

        user.updateLastLogin(
                credentials.getIpAddress(), java.time.LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
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

    public EmailLoginStrategy(
            UserGateway userGateway,
            VerificationCodeService verificationCodeService,
            @org.springframework.beans.factory.annotation.Qualifier("upmsClock") java.time.Clock clock) {
        this.clock = clock;
        this.userGateway = userGateway;
        this.verificationCodeService = verificationCodeService;
    }
}
