package io.github.loadup.components.gotone.channel.sms.config;

/*-
 * #%L
 * loadup-components-gotone-channel-sms
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
import io.github.loadup.components.gotone.channel.sms.AliyunSmsProvider;
import io.github.loadup.components.gotone.channel.sms.HuaweiSmsProvider;
import io.github.loadup.components.gotone.channel.sms.YunpianSmsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * SMS Channel Auto Configuration.
 *
 * <p>支持多个 SMS 提供商：
 * <ul>
 *   <li>aliyun - 阿里云短信</li>
 *   <li>huawei - 华为云短信</li>
 *   <li>yunpian - 云片短信</li>
 * </ul>
 */
@Slf4j
public class SmsChannelAutoConfiguration {

    /**
     * 阿里云短信提供商
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.sms.aliyun",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "aliyunSmsProvider")
    public NotificationChannelProvider aliyunSmsProvider() {
        log.info(">>> [GOTONE] AliyunSmsProvider initialized");
        return new AliyunSmsProvider();
    }

    /**
     * 华为云短信提供商
     */
    @Bean
    @ConditionalOnProperty(prefix = "loadup.gotone.sms.huawei", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "huaweiSmsProvider")
    public NotificationChannelProvider huaweiSmsProvider() {
        log.info(">>> [GOTONE] HuaweiSmsProvider initialized");
        return new HuaweiSmsProvider();
    }

    /**
     * 云片短信提供商
     */
    @Bean
    @ConditionalOnProperty(prefix = "loadup.gotone.sms.yunpian", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "yunpianSmsProvider")
    public NotificationChannelProvider yunpianSmsProvider() {
        log.info(">>> [GOTONE] YunpianSmsProvider initialized");
        return new YunpianSmsProvider();
    }
}
