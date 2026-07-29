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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Verification Code Service - For email/SMS verification
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Service
public class VerificationCodeService {
    private static final Logger log = LoggerFactory.getLogger(VerificationCodeService.class);


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

        VerificationCode verificationCode = VerificationCode.builder()
                .code(code)
                .target(target)
                .type(type)
                .attempts(0)
                .createdTime(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES))
                .build();

        codeStorage.put(key, verificationCode);

        log.info("Generated verification code for {}: {}", target, code);
        return code;
    }

    /**
     * Validate verification code
     */
    public boolean validateCode(String target, String type, String code) {
        String key = buildKey(target, type);
        VerificationCode verificationCode = codeStorage.get(key);

        if (verificationCode == null) {
            log.warn("Verification code not found for: {}", target);
            return false;
        }

        // Check expiry
        if (LocalDateTime.now().isAfter(verificationCode.getExpiryTime())) {
            log.warn("Verification code expired for: {}", target);
            codeStorage.remove(key);
            return false;
        }

        // Check attempts
        if (verificationCode.getAttempts() >= MAX_ATTEMPTS) {
            log.warn("Too many verification attempts for: {}", target);
            codeStorage.remove(key);
            return false;
        }

        // Validate code
        verificationCode.setAttempts(verificationCode.getAttempts() + 1);
        if (!code.equals(verificationCode.getCode())) {
            log.warn("Invalid verification code for: {}", target);
            return false;
        }

        // Code is valid, remove it
        codeStorage.remove(key);
        log.info("Verification code validated successfully for: {}", target);
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minResendTime = existingCode.getCreatedTime().plusMinutes(1);

        if (now.isBefore(minResendTime)) {
            log.warn("Too soon to resend code for: {}", target);
            return false;
        }

        return true;
    }

    /**
     * 验证短信验证码
     */
    public boolean verifySmsCode(String mobile, String code) {
        return validateCode(mobile, "SMS", code);
    }

    /**
     * 验证邮箱验证码
     */
    public boolean verifyEmailCode(String email, String code) {
        return validateCode(email, "EMAIL", code);
    }

    /**
     * 生成短信验证码
     */
    public String generateSmsCode(String mobile) {
        return generateCode(mobile, "SMS");
    }

    /**
     * 生成邮箱验证码
     */
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

    /**
     * Verification Code data class
     */
    private static class VerificationCode {
        private String code;
        private String target; // Email or phone
        private String type; // EMAIL or SMS
        private Integer attempts;
        private LocalDateTime createdTime;
        private LocalDateTime expiryTime;
    }

    public VerificationCodeService(Map<String, VerificationCode> codeStorage) {
        this.codeStorage = codeStorage;
    }

    public VerificationCodeService(Map<String, VerificationCode> codeStorage, String code, String target, String type, Integer attempts, LocalDateTime createdTime, LocalDateTime expiryTime) {
        this.codeStorage = codeStorage;
        this.code = code;
        this.target = target;
        this.type = type;
        this.attempts = attempts;
        this.createdTime = createdTime;
        this.expiryTime = expiryTime;
    }

    public VerificationCodeService() {
    }

    public Map<String, VerificationCode> getCodeStorage() {
        return this.codeStorage;
    }

    public String getCode() {
        return this.code;
    }

    public String getTarget() {
        return this.target;
    }

    public String getType() {
        return this.type;
    }

    public Integer getAttempts() {
        return this.attempts;
    }

    public LocalDateTime getCreatedTime() {
        return this.createdTime;
    }

    public LocalDateTime getExpiryTime() {
        return this.expiryTime;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(codeStorage, code, target, type, attempts, createdTime, expiryTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VerificationCodeService other = (VerificationCodeService) o;
        if (!java.util.Objects.equals(codeStorage, other.codeStorage)) return false;
        if (!java.util.Objects.equals(code, other.code)) return false;
        if (!java.util.Objects.equals(target, other.target)) return false;
        if (!java.util.Objects.equals(type, other.type)) return false;
        if (!java.util.Objects.equals(attempts, other.attempts)) return false;
        if (!java.util.Objects.equals(createdTime, other.createdTime)) return false;
        if (!java.util.Objects.equals(expiryTime, other.expiryTime)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "VerificationCodeService(" + "codeStorage=" + codeStorage + ", " + "code=" + code + ", " + "target=" + target + ", " + "type=" + type + ", " + "attempts=" + attempts + ", " + "createdTime=" + createdTime + ", " + "expiryTime=" + expiryTime + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, VerificationCode> codeStorage = new ConcurrentHashMap<>();
        private String code;
        private String target;
        private String type;
        private Integer attempts;
        private LocalDateTime createdTime;
        private LocalDateTime expiryTime;

        public Builder codeStorage(Map<String, VerificationCode> codeStorage) {
            this.codeStorage = codeStorage;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder target(String target) {
            this.target = target;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder attempts(Integer attempts) {
            this.attempts = attempts;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder expiryTime(LocalDateTime expiryTime) {
            this.expiryTime = expiryTime;
            return this;
        }

        public VerificationCodeService build() {
            return new VerificationCodeService(this.codeStorage, this.code, this.target, this.type, this.attempts, this.createdTime, this.expiryTime);
        }
    }
}
