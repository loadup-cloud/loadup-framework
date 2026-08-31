package io.github.loadup.components.gotone.channel.push.config;

/*-
 * #%L
 * Loadup Gotone Channel Push
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
