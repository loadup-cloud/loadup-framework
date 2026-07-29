package io.github.loadup.components.gotone.channel.webhook.config;

/*-
 * #%L
 * Loadup Gotone Channel Webhook
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
import io.github.loadup.components.gotone.channel.webhook.provider.DingtalkWebhookProvider;
import io.github.loadup.components.gotone.channel.webhook.provider.FeishuWebhookProvider;
import io.github.loadup.components.gotone.channel.webhook.provider.WechatWebhookProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class WebhookChannelAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(WebhookChannelAutoConfiguration.class);

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.binder.webhook.dingtalk",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "dingtalkWebhookProvider")
    public GotoneProvider dingtalkWebhookProvider() {
        log.info(">>> [GOTONE] DingtalkWebhookProvider initialized");
        return new DingtalkWebhookProvider();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.binder.webhook.wechat",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "wechatWebhookProvider")
    public GotoneProvider wechatWebhookProvider() {
        log.info(">>> [GOTONE] WechatWebhookProvider initialized");
        return new WechatWebhookProvider();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.binder.webhook.feishu",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "feishuWebhookProvider")
    public GotoneProvider feishuWebhookProvider() {
        log.info(">>> [GOTONE] FeishuWebhookProvider initialized");
        return new FeishuWebhookProvider();
    }
}
