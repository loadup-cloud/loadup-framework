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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Verification Code Service - For email/SMS verification
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

  private static final int CODE_LENGTH = 6;
  private static final int CODE_EXPIRY_MINUTES = 5;
  private static final int MAX_ATTEMPTS = 5;
  private static final Random RANDOM = new SecureRandom();

  // In-memory storage (for production, use Redis)
  private final Map<String, VerificationCode> codeStorage = new ConcurrentHashMap<>();

  /** Generate verification code */
  public String generateCode(String target, String type) {
    String code = generateRandomCode();
    String key = buildKey(target, type);

    VerificationCode verificationCode =
        VerificationCode.builder()
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

  /** Validate verification code */
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

  /** Check if code can be sent (rate limiting) */
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

  /** Build storage key */
  private String buildKey(String target, String type) {
    return type + ":" + target;
  }

  /** Generate random numeric code */
  private String generateRandomCode() {
    StringBuilder code = new StringBuilder();
    for (int i = 0; i < CODE_LENGTH; i++) {
      code.append(RANDOM.nextInt(10));
    }
    return code.toString();
  }

  /** Verification Code data class */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  private static class VerificationCode {
    private String code;
    private String target; // Email or phone
    private String type; // EMAIL or SMS
    private Integer attempts;
    private LocalDateTime createdTime;
    private LocalDateTime expiryTime;
  }
}
