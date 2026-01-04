package com.github.loadup.modules.upms.app.service;

import com.github.loadup.modules.upms.app.command.UserPasswordResetCommand;
import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.domain.repository.UserRepository;
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

  private final UserRepository userRepository;
  private final VerificationCodeService verificationCodeService;
  private final PasswordEncoder passwordEncoder;

  /** Send verification code via email */
  public void sendEmailVerificationCode(String email) {
    // Validate email exists
    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("该邮箱未注册"));

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
  public void sendSmsVerificationCode(String phone) {
    // Validate phone exists
    User user =
        userRepository.findByPhone(phone).orElseThrow(() -> new RuntimeException("该手机号未注册"));

    // Check rate limiting
    if (!verificationCodeService.canSendCode(phone, "SMS")) {
      throw new RuntimeException("验证码发送过于频繁，请稍后再试");
    }

    // Generate code
    String code = verificationCodeService.generateCode(phone, "SMS");

    // TODO: Send SMS using loadup-components-gotone
    log.info("Sending SMS verification code to {}: {}", phone, code);
  }

  /** Reset password with verification code */
  @Transactional
  public void resetPassword(UserPasswordResetCommand command) {
    // Find user
    User user =
        userRepository
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
      if (user.getPhone() == null) {
        throw new RuntimeException("该用户未绑定手机号");
      }
      target = user.getPhone();
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

    userRepository.update(user);
    log.info("Password reset successful for user: {}", user.getUsername());
  }
}
