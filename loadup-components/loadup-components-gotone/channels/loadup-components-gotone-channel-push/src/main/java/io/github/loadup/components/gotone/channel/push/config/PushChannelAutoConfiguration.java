package io.github.loadup.components.gotone.channel.push.config;

/*-
 * #%L
 * Loadup Gotone Channel Push
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

import io.github.loadup.components.gotone.GotoneProvider;
import io.github.loadup.components.gotone.channel.push.FcmPushConfig;
import io.github.loadup.components.gotone.channel.push.FcmPushProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(
        prefix = "loadup.gotone.binder.push.fcm",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(FcmPushConfig.class)
public class PushChannelAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(PushChannelAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(name = "fcmPushProvider")
    public GotoneProvider fcmPushProvider(FcmPushConfig config) {
        log.info(">>> [GOTONE] FcmPushProvider initialized with projectId={}", config.getProjectId());
        return new FcmPushProvider(config.getServerKey(), config.getProjectId());
    }
}
