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

import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.upms.api.command.UserPasswordResetCommand;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Password Reset Service - Handles password reset with email/SMS verification
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

  private final UserGateway userGateway;
  private final VerificationCodeService verificationCodeService;
  private final PasswordEncoder passwordEncoder;

  /** Send verification code via email */
  public void sendEmailVerificationCode(String email) {
    // Validate email exists
    User user = userGateway.findByEmail(email).orElseThrow(() -> new RuntimeException("该邮箱未注册"));

    // Check rate limiting
    if (!verificationCodeService.canSendCode(email, "EMAIL")) {
      throw new RuntimeException("验证码发送过于频繁，请稍后再试");
    }

    // Generate code
    String code = verificationCodeService.generateCode(email, "EMAIL");

    // TODO: Send email using loadup-components-gotone
    log.info("Sending email verification code to {}: {}", email, code);
  }

  /** Send verification code via SMS */
  public void sendSmsVerificationCode(String mobile) {
    // Validate mobile exists
    User user = userGateway.findByMobile(mobile).orElseThrow(() -> new RuntimeException("该手机号未注册"));

    // Check rate limiting
    if (!verificationCodeService.canSendCode(mobile, "SMS")) {
      throw new RuntimeException("验证码发送过于频繁，请稍后再试");
    }

    // Generate code
    String code = verificationCodeService.generateCode(mobile, "SMS");

    // TODO: Send SMS using loadup-components-gotone
    log.info("Sending SMS verification code to {}: {}", mobile, code);
  }

  /** Reset password with verification code */
  @Transactional
  public void resetPassword(UserPasswordResetCommand command) {
    // Find user
    User user =
        userGateway
            .findByUsername(command.getUsername())
            .orElseThrow(() -> new RuntimeException("用户不存在"));

    // Determine verification target (email or phone)
    String target;
    String type = command.getVerificationType();

    if ("EMAIL".equalsIgnoreCase(type)) {
      if (user.getEmail() == null) {
        throw new RuntimeException("该用户未绑定邮箱");
      }
      target = user.getEmail();
    } else if ("SMS".equalsIgnoreCase(type)) {
      if (user.getMobile() == null) {
        throw new RuntimeException("该用户未绑定手机号");
      }
      target = user.getMobile();
    } else {
      throw new RuntimeException("不支持的验证方式");
    }

    // Validate verification code
    if (!verificationCodeService.validateCode(target, type, command.getVerificationCode())) {
      throw new RuntimeException("验证码无效或已过期");
    }

    // Check new password confirmation
    if (!command.getNewPassword().equals(command.getConfirmPassword())) {
      throw new RuntimeException("两次输入的密码不一致");
    }

    // Reset password
    user.setPassword(passwordEncoder.encode(command.getNewPassword()));
    user.setPasswordUpdateTime(LocalDateTime.now());
    user.setUpdatedTime(LocalDateTime.now());

    userGateway.update(user);
    log.info("Password reset successful for user: {}", user.getUsername());
  }
}
