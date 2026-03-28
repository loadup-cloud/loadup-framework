package io.github.loadup.components.gotone.channel.email.config;

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
import io.github.loadup.components.gotone.channel.email.SmtpEmailProvider;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Email Channel Auto Configuration.
 */
@Slf4j
@ConditionalOnProperty(
        prefix = "loadup.gotone.email.smtp",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class EmailChannelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender(
            @org.springframework.beans.factory.annotation.Value("${spring.mail.host:smtp.gmail.com}") String host,
            @org.springframework.beans.factory.annotation.Value("${spring.mail.port:587}") int port,
            @org.springframework.beans.factory.annotation.Value("${spring.mail.username:}") String username,
            @org.springframework.beans.factory.annotation.Value("${spring.mail.password:}") String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.email.smtp",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "smtpEmailProvider")
    public NotificationChannelProvider smtpEmailProvider(JavaMailSender mailSender) {
        log.info(">>> [GOTONE] SmtpEmailProvider initialized");
        return new SmtpEmailProvider(mailSender);
    }
}
