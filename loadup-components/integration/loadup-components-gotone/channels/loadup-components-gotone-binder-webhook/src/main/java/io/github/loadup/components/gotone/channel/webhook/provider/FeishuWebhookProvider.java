package io.github.loadup.components.gotone.channel.webhook.provider;

import static io.github.loadup.components.gotone.channel.webhook.provider.WebhookSupport.configValue;
import static io.github.loadup.components.gotone.channel.webhook.provider.WebhookSupport.maskUrl;
import static io.github.loadup.components.gotone.channel.webhook.provider.WebhookSupport.postJson;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;

/**
 * Feishu custom bot webhook provider.
 *
 * <p>Channel-level configuration keys: {@code webhookUrl} (required), {@code title}.
 */
public class FeishuWebhookProvider implements NotificationChannelProvider {

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
            LogUtil.info(FeishuWebhookProvider.class, "Feishu webhook sent to {}", maskUrl(webhookUrl));
            return success(request);
        } catch (Exception e) {
            LogUtil.warn(FeishuWebhookProvider.class, "Feishu webhook failed for {}", maskUrl(webhookUrl), e);
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
