package io.github.loadup.components.gotone.channel.webhook.config;

/*-
 * #%L
 * Loadup Gotone Channel Webhook
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
