/*-
 * #%L
 * Loadup Gotone Binder Webhook
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
package io.github.loadup.components.gotone.channel.webhook.provider;

import static io.github.loadup.components.gotone.channel.webhook.provider.WebhookSupport.configValue;
import static io.github.loadup.components.gotone.channel.webhook.provider.WebhookSupport.maskUrl;
import static io.github.loadup.components.gotone.channel.webhook.provider.WebhookSupport.postJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feishu custom bot webhook provider.
 *
 * <p>Channel-level configuration keys: {@code webhookUrl} (required), {@code title}.
 */
public class FeishuWebhookProvider implements NotificationChannelProvider {

    private static final Logger log = LoggerFactory.getLogger(FeishuWebhookProvider.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public String getChannelType() {
        return "WEBHOOK";
    }

    @Override
    public String getProviderName() {
        return "feishu";
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        String webhookUrl = configValue(request.channelConfig(), "webhookUrl", null);
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return failure(request, "webhookUrl is not configured");
        }

        try {
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("text", request.content());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("msg_type", "text");
            payload.put("content", content);

            boolean success = postJson(webhookUrl, JSON.writeValueAsString(payload));
            if (!success) {
                return failure(request, "webhook endpoint returned a non-2xx status");
            }
            log.info("Feishu webhook sent to {}", maskUrl(webhookUrl));
            return success(request);
        } catch (Exception e) {
            log.warn("Feishu webhook failed for {}", maskUrl(webhookUrl), e);
            return failure(request, e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private static ChannelSendResponse success(ChannelSendRequest request) {
        return new ChannelSendResponse(request.content(), 1, 0, Map.of("feishu", true), Map.of());
    }

    private static ChannelSendResponse failure(ChannelSendRequest request, String error) {
        return new ChannelSendResponse(request.content(), 0, 1, Map.of("feishu", false), Map.of("feishu", error));
    }
}
