package io.github.loadup.components.gotone.binder.email.config;

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
import io.github.loadup.components.gotone.binder.email.SmtpEmailProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Email Channel Auto Configuration.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnBean(JavaMailSender.class)
public class EmailChannelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "smtpEmailProvider")
    public NotificationChannelProvider smtpEmailProvider(JavaMailSender mailSender) {
        log.info(">>> [GOTONE] SmtpEmailProvider initialized");
        return new SmtpEmailProvider(mailSender);
    }
}
