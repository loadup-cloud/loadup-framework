package io.github.loadup.components.gotone.binder.email;

/*-
 * #%L
 * loadup-components-gotone-channel-email
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

import io.github.loadup.components.gotone.api.NotificationChannelProvider;
import io.github.loadup.components.gotone.enums.NotificationChannel;
import io.github.loadup.components.gotone.enums.NotificationStatus;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import io.github.loadup.components.gotone.model.NotificationResponse;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/** SMTP Email Channel Provider */
@Slf4j
@RequiredArgsConstructor
public class SmtpEmailProvider implements NotificationChannelProvider {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {


        if (mailSender == null) {
            return buildErrorResponse("JavaMailSender not configured");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(request.getReceivers().toArray(new String[0]));
            helper.setSubject(request.getContent() != null ? request.getContent() : "Notification");
            helper.setText(request.getContent(), true);

            mailSender.send(message);

            log.info(">>> [GOTONE] Email sent successfully via SMTP to: {}", request.getReceivers());

            return ChannelSendResponse.builder()
                   .build();

        } catch (Exception e) {
            log.error(">>> [GOTONE] Failed to send email via SMTP", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return mailSender != null;
    }

    @Override
    public String getProviderName() {
        return "smtp";
    }

    private ChannelSendResponse buildErrorResponse(String errorMessage) {
        return ChannelSendResponse.builder()
               .build();
    }
}
