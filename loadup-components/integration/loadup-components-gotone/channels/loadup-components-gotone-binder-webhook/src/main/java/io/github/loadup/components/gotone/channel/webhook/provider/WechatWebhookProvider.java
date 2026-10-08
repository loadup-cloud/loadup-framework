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
 * WeChat Work group robot webhook provider.
 *
 * <p>Channel-level configuration keys: {@code webhookUrl} (required), {@code mentionedList},
 * {@code mentionedMobileList}. Receivers default to {@code mentionedMobileList}.
 */
public class WechatWebhookProvider implements NotificationChannelProvider {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public String getChannelType() {
        return "WEBHOOK";
    }

    @Override
    public String getProviderName() {
        return "wechat";
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        String webhookUrl = configValue(request.channelConfig(), "webhookUrl", null);
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return failure(request, "webhookUrl is not configured");
        }

        try {
            Map<String, Object> text = new LinkedHashMap<>();
            text.put("content", request.content());
            if (request.channelConfig().containsKey("mentionedList")) {
                text.put("mentioned_list", request.channelConfig().get("mentionedList"));
            } else if (request.channelConfig().containsKey("mentionedMobileList")) {
                text.put("mentioned_mobile_list", request.channelConfig().get("mentionedMobileList"));
            } else {
                text.put("mentioned_mobile_list", request.receivers());
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("msgtype", "text");
            payload.put("text", text);

            boolean success = postJson(webhookUrl, JSON.writeValueAsString(payload));
            if (!success) {
                return failure(request, "webhook endpoint returned a non-2xx status");
            }
            LogUtil.info(WechatWebhookProvider.class, "WeChat webhook sent to {}", maskUrl(webhookUrl));
            return success(request);
        } catch (Exception e) {
            LogUtil.warn(WechatWebhookProvider.class, "WeChat webhook failed for {}", maskUrl(webhookUrl), e);
            return failure(request, e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private static ChannelSendResponse success(ChannelSendRequest request) {
        return new ChannelSendResponse(request.content(), 1, 0, Map.of("wechat", true), Map.of());
    }

    private static ChannelSendResponse failure(ChannelSendRequest request, String error) {
        return new ChannelSendResponse(request.content(), 0, 1, Map.of("wechat", false), Map.of("wechat", error));
    }
}
