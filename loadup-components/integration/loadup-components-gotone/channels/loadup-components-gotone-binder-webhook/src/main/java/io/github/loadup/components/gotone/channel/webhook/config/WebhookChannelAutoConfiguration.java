/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.gotone.channel.webhook.config;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.channel.webhook.provider.DingtalkWebhookProvider;
import io.github.loadup.components.gotone.channel.webhook.provider.FeishuWebhookProvider;
import io.github.loadup.components.gotone.channel.webhook.provider.WechatWebhookProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

@AutoConfiguration
public class WebhookChannelAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.binder.webhook.dingtalk",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "dingtalkWebhookProvider")
    public NotificationChannelProvider dingtalkWebhookProvider(JsonMapper mapper) {
        return new DingtalkWebhookProvider(mapper);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.binder.webhook.wechat",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "wechatWebhookProvider")
    public NotificationChannelProvider wechatWebhookProvider(JsonMapper mapper) {
        return new WechatWebhookProvider(mapper);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.gotone.binder.webhook.feishu",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "feishuWebhookProvider")
    public NotificationChannelProvider feishuWebhookProvider(JsonMapper mapper) {
        return new FeishuWebhookProvider(mapper);
    }
}
