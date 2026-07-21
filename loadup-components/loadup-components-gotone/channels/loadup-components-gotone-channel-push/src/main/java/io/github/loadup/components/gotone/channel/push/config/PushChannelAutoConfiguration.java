package io.github.loadup.components.gotone.channel.push.config;

/*-
 * #%L
 * loadup-components-gotone-channel-push
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
import io.github.loadup.components.gotone.channel.push.FcmPushProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Push Channel Auto Configuration.
 *
 * <p>支持 PUSH 提供商：
 * <ul>
 *   <li>fcm - Firebase Cloud Messaging</li>
 * </ul>
 */
@Slf4j
public class PushChannelAutoConfiguration {

    /**
     * Firebase Cloud Messaging 推送提供商
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.push.fcm",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "fcmPushProvider")
    public NotificationChannelProvider fcmPushProvider() {
        log.info(">>> [GOTONE] FcmPushProvider initialized");
        return new FcmPushProvider();
    }
}
