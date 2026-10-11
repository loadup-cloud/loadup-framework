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

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.masking.MaskType;
import io.github.loadup.commons.masking.Masking;
import io.github.loadup.modules.upms.domain.model.VerificationCode;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Verification Code Service - For email/SMS verification
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Service
public class VerificationCodeService {
    private final Clock clock;

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final Random RANDOM = new SecureRandom();

    // In-memory storage (for production, use Redis)
    private final Map<String, VerificationCode> codeStorage = new ConcurrentHashMap<>();

    /**
     * Generate verification code
     */
    public String generateCode(String target, String type) {
        String code = generateRandomCode();
        String key = buildKey(target, type);

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(code);
        verificationCode.setTarget(target);
        verificationCode.setType(type);
        verificationCode.setAttempts(0);
        verificationCode.setCreatedAt(LocalDateTime.ofInstant(clock.instant(), java.time.ZoneOffset.UTC));

        verificationCode.setExpiryTime(LocalDateTime.ofInstant(clock.instant(), java.time.ZoneOffset.UTC)
                .plusMinutes(CODE_EXPIRY_MINUTES));

        codeStorage.put(key, verificationCode);

        LogUtil.info(
                VerificationCodeService.class,
                "Generated verification code for {}",
                Masking.mask(target, type.equalsIgnoreCase("EMAIL") ? MaskType.EMAIL : MaskType.PHONE));
        return code;
    }

    /**
     * Validate verification code
     */
    public boolean validateCode(String target, String type, String code) {
        String key = buildKey(target, type);
        VerificationCode verificationCode = codeStorage.get(key);

        if (verificationCode == null) {
            LogUtil.warn(
                    VerificationCodeService.class, "Verification code not found for: {}", Masking.keep(target, 1, 1));
            return false;
        }

        // Check expiry
        if (!LocalDateTime.ofInstant(clock.instant(), java.time.ZoneOffset.UTC)
                .isBefore(verificationCode.getExpiryTime())) {
            LogUtil.warn(
                    VerificationCodeService.class, "Verification code expired for: {}", Masking.keep(target, 1, 1));
            codeStorage.remove(key);
            return false;
        }

        // Check attempts
        if (verificationCode.getAttempts() >= MAX_ATTEMPTS) {
            LogUtil.warn(
                    VerificationCodeService.class,
                    "Too many verification attempts for: {}",
                    Masking.keep(target, 1, 1));
            codeStorage.remove(key);
            return false;
        }

        // Validate code
        verificationCode.setAttempts(verificationCode.getAttempts() + 1);
        if (!code.equals(verificationCode.getCode())) {
            LogUtil.warn(
                    VerificationCodeService.class, "Invalid verification code for: {}", Masking.keep(target, 1, 1));
            return false;
        }

        // Code is valid, remove it
        codeStorage.remove(key);
        LogUtil.info(
                VerificationCodeService.class,
                "Verification code validated successfully for: {}",
                Masking.keep(target, 1, 1));
        return true;
    }

    /**
     * Check if code can be sent (rate limiting)
     */
    public boolean canSendCode(String target, String type) {
        String key = buildKey(target, type);
        VerificationCode existingCode = codeStorage.get(key);

        if (existingCode == null) {
            return true;
        }

        // Check if previous code is still valid (prevent spamming)
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), java.time.ZoneOffset.UTC);
        LocalDateTime minResendTime = existingCode.getCreatedAt().plusMinutes(1);

        if (now.isBefore(minResendTime)) {
            LogUtil.warn(VerificationCodeService.class, "Too soon to resend code for: {}", Masking.keep(target, 1, 1));
            return false;
        }

        return true;
    }

    public boolean verifySmsCode(String mobile, String code) {
        return validateCode(mobile, "SMS", code);
    }

    public boolean verifyEmailCode(String email, String code) {
        return validateCode(email, "EMAIL", code);
    }

    public String generateSmsCode(String mobile) {
        return generateCode(mobile, "SMS");
    }

    public String generateEmailCode(String email) {
        return generateCode(email, "EMAIL");
    }

    /**
     * Build storage key
     */
    private String buildKey(String target, String type) {
        return type + ":" + target;
    }

    /**
     * Generate random numeric code
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }

    public VerificationCodeService(@Qualifier("upmsClock") Clock clock) {
        this.clock = clock;
    }
}
