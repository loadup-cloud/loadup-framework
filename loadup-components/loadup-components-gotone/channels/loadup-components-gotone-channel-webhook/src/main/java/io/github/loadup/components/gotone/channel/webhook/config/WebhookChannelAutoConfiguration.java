package io.github.loadup.components.gotone.channel.webhook.config;

/*-
 * #%L
 * loadup-components-gotone-channel-webhook
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
import io.github.loadup.components.gotone.channel.webhook.provider.DingtalkWebhookProvider;
import io.github.loadup.components.gotone.channel.webhook.provider.FeishuWebhookProvider;
import io.github.loadup.components.gotone.channel.webhook.provider.WechatWebhookProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Webhook Channel Auto Configuration.
 *
 * <p>支持 Webhook 提供商：
 * <ul>
 *   <li>dingtalk - 钉钉机器人</li>
 *   <li>wechat - 企业微信机器人</li>
 *   <li>feishu - 飞书机器人</li>
 * </ul>
 */
@Slf4j
public class WebhookChannelAutoConfiguration {

    /**
     * 钉钉 Webhook 提供商
     */
    @Bean
    @ConditionalOnProperty(prefix = "loadup.gotone.webhook.dingtalk", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "dingtalkWebhookProvider")
    public NotificationChannelProvider dingtalkWebhookProvider() {
        log.info(">>> [GOTONE] DingtalkWebhookProvider initialized");
        return new DingtalkWebhookProvider();
    }

    /**
     * 企业微信 Webhook 提供商
     */
    @Bean
    @ConditionalOnProperty(prefix = "loadup.gotone.webhook.wechat", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "wechatWebhookProvider")
    public NotificationChannelProvider wechatWebhookProvider() {
        log.info(">>> [GOTONE] WechatWebhookProvider initialized");
        return new WechatWebhookProvider();
    }

    /**
     * 飞书 Webhook 提供商
     */
    @Bean
    @ConditionalOnProperty(prefix = "loadup.gotone.webhook.feishu", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "feishuWebhookProvider")
    public NotificationChannelProvider feishuWebhookProvider() {
        log.info(">>> [GOTONE] FeishuWebhookProvider initialized");
        return new FeishuWebhookProvider();
    }
}

