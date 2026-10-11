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
import io.github.loadup.commons.masking.MaskType;
import io.github.loadup.commons.masking.Masking;
import io.github.loadup.modules.upms.client.command.UserPasswordResetCommand;
import io.github.loadup.modules.upms.client.validation.PasswordPolicy;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.model.User;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Password Reset Service - Handles password reset with email/SMS verification
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Service
public class PasswordResetService implements io.github.loadup.modules.upms.client.facade.PasswordResetFacade {
    private final Clock clock;

    private final UserGateway userGateway;
    private final VerificationCodeService verificationCodeService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Send verification code via email
     */
    public void sendEmailVerificationCode(String email) {
        // Validate email exists
        userGateway
                .findByEmail(email)
                .orElseThrow(() -> new CommonException(CommonResultCodeEnum.NOT_FOUND, "Email is not registered"));

        // Check rate limiting
        if (!verificationCodeService.canSendCode(email, "EMAIL")) {
            throw new CommonException(CommonResultCodeEnum.RATE_LIMITED, "Verification requests are rate limited");
        }

        // Generate code
        String code = verificationCodeService.generateCode(email, "EMAIL");

        // TODO: Send email using loadup-components-gotone
        LogUtil.info(
                PasswordResetService.class,
                "Email verification code generated for {}",
                Masking.mask(email, MaskType.EMAIL));
    }

    /**
     * Send verification code via SMS
     */
    public void sendSmsVerificationCode(String mobile) {
        // Validate mobile exists
        userGateway
                .findByMobile(mobile)
                .orElseThrow(
                        () -> new CommonException(CommonResultCodeEnum.NOT_FOUND, "Phone number is not registered"));

        // Check rate limiting
        if (!verificationCodeService.canSendCode(mobile, "SMS")) {
            throw new CommonException(CommonResultCodeEnum.RATE_LIMITED, "Verification requests are rate limited");
        }

        // Generate code
        String code = verificationCodeService.generateCode(mobile, "SMS");

        // TODO: Send SMS using loadup-components-gotone
        LogUtil.info(
                PasswordResetService.class,
                "SMS verification code generated for {}",
                Masking.mask(mobile, MaskType.PHONE));
    }

    /**
     * Reset password with verification code
     */
    @Transactional
    public void resetPassword(UserPasswordResetCommand command) {
        if (command == null) throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        PasswordPolicy.requireValid(command.getNewPassword());
        // Check new password confirmation
        if (!command.getNewPassword().equals(command.getConfirmPassword())) {
            throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL, "Password confirmation does not match");
        }

        // Find user
        User user = userGateway
                .findByUsername(command.getUsername())
                .orElseThrow(() -> new CommonException(CommonResultCodeEnum.NOT_FOUND, "User not found"));

        // Determine verification target (email or phone)
        String target;
        String type = command.getVerificationType();

        if ("EMAIL".equalsIgnoreCase(type)) {
            if (user.getEmail() == null) {
                throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL, "Email is not bound to the user");
            }
            target = user.getEmail();
        } else if ("SMS".equalsIgnoreCase(type)) {
            if (user.getMobile() == null) {
                throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL, "Phone number is not bound to the user");
            }
            target = user.getMobile();
        } else {
            throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL, "Unsupported verification method");
        }

        // Validate verification code
        if (!verificationCodeService.validateCode(target, type, command.getVerificationCode())) {
            throw new CommonException(CommonResultCodeEnum.UNAUTHENTICATED, "Verification code is invalid or expired");
        }

        // Reset password
        user.setPassword(passwordEncoder.encode(command.getNewPassword()));
        user.setPasswordUpdateTime(LocalDateTime.ofInstant(clock.instant(), java.time.ZoneOffset.UTC));

        userGateway.update(user);
        LogUtil.info(PasswordResetService.class, "Password reset successful for user: {}", user.getUsername());
    }

    public PasswordResetService(
            UserGateway userGateway,
            VerificationCodeService verificationCodeService,
            PasswordEncoder passwordEncoder,
            @Qualifier("upmsClock") Clock clock) {
        this.clock = clock;
        this.userGateway = userGateway;
        this.verificationCodeService = verificationCodeService;
        this.passwordEncoder = passwordEncoder;
    }
}
