/*-
 * #%L
 * Loadup Gotone Binder Email
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
package io.github.loadup.components.gotone.channel.email;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * SMTP email provider backed by Spring Mail.
 *
 * <p>Per-receiver sending keeps a precise per-receiver success/failure status. Channel-level
 * configuration keys: {@code subject}, {@code from}, {@code html}.
 */
public class SmtpEmailProvider implements NotificationChannelProvider {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailProvider.class);

    private final JavaMailSender mailSender;
    private final String defaultFrom;

    public SmtpEmailProvider(JavaMailSender mailSender, String defaultFrom) {
        this.mailSender = mailSender;
        this.defaultFrom = defaultFrom;
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }

    @Override
    public String getProviderName() {
        return "smtp";
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        String subject = configValue(request.channelConfig(), "subject", "Notification");
        String from = configValue(request.channelConfig(), "from", defaultFrom);
        boolean html = Boolean.parseBoolean(configValue(request.channelConfig(), "html", "true"));

        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();
        int successCount = 0;

        for (String receiver : request.receivers()) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(from);
                helper.setTo(receiver);
                helper.setSubject(subject);
                helper.setText(request.content(), html);
                mailSender.send(message);
                receiverStatus.put(receiver, true);
                successCount++;
            } catch (Exception e) {
                receiverStatus.put(receiver, false);
                receiverErrors.put(receiver, e.getMessage());
                log.warn("Email send failed for receiver={}", maskEmail(receiver), e);
            }
        }

        return new ChannelSendResponse(
                request.content(),
                successCount,
                request.receivers().size() - successCount,
                receiverStatus,
                receiverErrors);
    }

    @Override
    public boolean isAvailable() {
        return mailSender != null;
    }

    private static String configValue(Map<String, Object> config, String key, String defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private static String maskEmail(String email) {
        if (email == null || email.length() < 4) {
            return "***";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.substring(0, 2) + "***" + email.substring(at);
    }
}
