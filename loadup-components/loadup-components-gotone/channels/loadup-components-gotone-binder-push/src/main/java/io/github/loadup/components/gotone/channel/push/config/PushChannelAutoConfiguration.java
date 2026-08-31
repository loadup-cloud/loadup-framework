/*-
 * #%L
 * Loadup Gotone Binder Push
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
package io.github.loadup.components.gotone.channel.push.config;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.channel.push.FcmPushConfig;
import io.github.loadup.components.gotone.channel.push.FcmPushProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the Firebase Cloud Messaging push binder.
 */
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "loadup.gotone.binder.push.fcm",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(FcmPushConfig.class)
public class PushChannelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "fcmPushProvider")
    public NotificationChannelProvider fcmPushProvider(FcmPushConfig config) {
        return new FcmPushProvider(config.getServerKey(), config.getProjectId());
    }
}
