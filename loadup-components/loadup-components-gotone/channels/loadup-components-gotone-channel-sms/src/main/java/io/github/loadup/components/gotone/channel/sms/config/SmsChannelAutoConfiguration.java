package io.github.loadup.components.gotone.channel.sms.config;

/*-
 * #%L
 * Loadup Gotone Channel SMS
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
import io.github.loadup.components.gotone.channel.sms.AliyunSmsConfig;
import io.github.loadup.components.gotone.channel.sms.AliyunSmsProvider;
import io.github.loadup.components.gotone.channel.sms.HuaweiSmsConfig;
import io.github.loadup.components.gotone.channel.sms.HuaweiSmsProvider;
import io.github.loadup.components.gotone.channel.sms.YunpianSmsConfig;
import io.github.loadup.components.gotone.channel.sms.YunpianSmsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties({AliyunSmsConfig.class, HuaweiSmsConfig.class, YunpianSmsConfig.class})
public class SmsChannelAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SmsChannelAutoConfiguration.class);

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.binder.sms.aliyun",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "aliyunSmsProvider")
    public GotoneProvider aliyunSmsProvider(AliyunSmsConfig config) {
        log.info(">>> [GOTONE] AliyunSmsProvider initialized");
        return new AliyunSmsProvider(
                config.getAccessKeyId(), config.getAccessKeySecret(), config.getSignName(), config.getRegionId());
    }

    @Bean
    @ConditionalOnProperty(prefix = "loadup.gotone.binder.sms.huawei", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "huaweiSmsProvider")
    public GotoneProvider huaweiSmsProvider(HuaweiSmsConfig config) {
        log.info(">>> [GOTONE] HuaweiSmsProvider initialized");
        return new HuaweiSmsProvider(
                config.getAppKey(),
                config.getAppSecret(),
                config.getSender(),
                config.getSignature(),
                config.getEndpoint());
    }

    @Bean
    @ConditionalOnProperty(prefix = "loadup.gotone.binder.sms.yunpian", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "yunpianSmsProvider")
    public GotoneProvider yunpianSmsProvider(YunpianSmsConfig config) {
        log.info(">>> [GOTONE] YunpianSmsProvider initialized");
        return new YunpianSmsProvider(config.getApiKey(), config.getApiUrl());
    }
}
