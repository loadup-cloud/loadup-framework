/*-
 * #%L
 * Loadup Gotone Binder SMS
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
package io.github.loadup.components.gotone.channel.sms.config;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.channel.sms.AliyunSmsConfig;
import io.github.loadup.components.gotone.channel.sms.AliyunSmsProvider;
import io.github.loadup.components.gotone.channel.sms.HuaweiSmsConfig;
import io.github.loadup.components.gotone.channel.sms.HuaweiSmsProvider;
import io.github.loadup.components.gotone.channel.sms.YunpianSmsConfig;
import io.github.loadup.components.gotone.channel.sms.YunpianSmsProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the Aliyun / Huawei / Yunpian SMS binders.
 */
@AutoConfiguration
@EnableConfigurationProperties({AliyunSmsConfig.class, HuaweiSmsConfig.class, YunpianSmsConfig.class})
public class SmsChannelAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.binder.sms.aliyun",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "aliyunSmsProvider")
    public NotificationChannelProvider aliyunSmsProvider(AliyunSmsConfig config) {
        return new AliyunSmsProvider(
                config.getAccessKeyId(), config.getAccessKeySecret(), config.getSignName(), config.getRegionId());
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.binder.sms.huawei",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "huaweiSmsProvider")
    public NotificationChannelProvider huaweiSmsProvider(HuaweiSmsConfig config) {
        return new HuaweiSmsProvider(
                config.getAppKey(),
                config.getAppSecret(),
                config.getSender(),
                config.getSignature(),
                config.getEndpoint());
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.binder.sms.yunpian",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "yunpianSmsProvider")
    public NotificationChannelProvider yunpianSmsProvider(YunpianSmsConfig config) {
        return new YunpianSmsProvider(config.getApiKey(), config.getApiUrl());
    }
}
