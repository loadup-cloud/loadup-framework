package io.github.loadup.components.gotone.binder.email;

/*-
 * #%L
 * loadup-components-gotone-binder-email
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import io.github.loadup.components.extension.annotation.Extension;
import io.github.loadup.components.gotone.api.INotificationProvider;
import io.github.loadup.components.gotone.enums.NotificationStatus;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/** SMTP 邮件提供商实现 */
@Slf4j
@Component
@Extension(bizCode = "EMAIL", useCase = "smtp")
public class SmtpEmailProvider implements INotificationProvider {

  @Autowired(required = false)
  private JavaMailSender mailSender;

  @Value("${spring.mail.username:}")
  private String fromEmail;

  @Override
  public NotificationResponse send(NotificationRequest request) {
    if (mailSender == null) {
      return buildErrorResponse(request, "JavaMailSender not configured");
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(request.getReceivers().toArray(new String[0]));
      helper.setSubject(request.getTitle() != null ? request.getTitle() : "Notification");
      helper.setText(request.getContent(), true);

      mailSender.send(message);

      log.info("Email sent successfully via SMTP to: {}", request.getReceivers());

      return NotificationResponse.builder()
          .success(true)
          .status(NotificationStatus.SUCCESS)
          .bizId(request.getBizId())
          .messageId(UUID.randomUUID().toString())
          .provider(getProviderName())
          .sendTime(LocalDateTime.now())
          .build();

    } catch (Exception e) {
      log.error("Failed to send email via SMTP: {}", e.getMessage(), e);
      return buildErrorResponse(request, e.getMessage());
    }
  }

  @Override
  public String getProviderName() {
    return "smtp";
  }

  @Override
  public boolean isAvailable() {
    return mailSender != null;
  }

  private NotificationResponse buildErrorResponse(
      NotificationRequest request, String errorMessage) {
    return NotificationResponse.builder()
        .success(false)
        .status(NotificationStatus.FAILED)
        .bizId(request.getBizId())
        .provider(getProviderName())
        .errorMessage(errorMessage)
        .sendTime(LocalDateTime.now())
        .build();
  }
}
