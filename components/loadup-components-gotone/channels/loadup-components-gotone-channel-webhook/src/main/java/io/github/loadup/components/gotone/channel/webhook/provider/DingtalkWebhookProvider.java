package io.github.loadup.components.gotone.channel.webhook.provider;

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
import io.github.loadup.components.gotone.enums.NotificationChannel;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 钉钉机器人 Webhook 提供商
 *
 * <p>配置示例：
 * <pre>
 * # 渠道配置（channelConfig）中需要包含：
 * {
 *   "webhookUrl": "https://oapi.dingtalk.com/robot/send?access_token=xxx",
 *   "secret": "SEC...",  # 可选，加签密钥
 *   "msgtype": "text",   # text/markdown/link/actionCard
 *   "atMobiles": ["13800138000"],  # @指定人的手机号
 *   "atAll": false       # 是否@所有人
 * }
 * </pre>
 *
 * @see <a href="https://open.dingtalk.com/document/robots/custom-robot-access">钉钉自定义机器人</a>
 */
@Slf4j
public class DingtalkWebhookProvider implements NotificationChannelProvider {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WEBHOOK;
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        log.info(
                ">>> [GOTONE-WEBHOOK-DINGTALK] 开始发送钉钉消息, receivers={}, content length={}",
                request.getReceivers(),
                request.getContent() != null ? request.getContent().length() : 0);

        // 参数校验
        if (request.getChannelConfig() == null || !request.getChannelConfig().containsKey("webhookUrl")) {
            return buildErrorResponse("Webhook URL 未配置", 0, 0);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return buildErrorResponse("消息内容为空", 1, 0);
        }

        String webhookUrl = getConfigValue(request.getChannelConfig(), "webhookUrl", null);
        String secret = getConfigValue(request.getChannelConfig(), "secret", null);
        String msgType = getConfigValue(request.getChannelConfig(), "msgtype", "text");
        boolean atAll = Boolean.parseBoolean(getConfigValue(request.getChannelConfig(), "atAll", "false"));

        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();

        try {
            // TODO: 实际集成钉钉 Webhook API
            // 示例代码：
            // if (secret != null && !secret.isEmpty()) {
            //     // 计算签名
            //     long timestamp = System.currentTimeMillis();
            //     String stringToSign = timestamp + "\n" + secret;
            //     Mac mac = Mac.getInstance("HmacSHA256");
            //     mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            //     byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            //     String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
            //     webhookUrl = webhookUrl + "&timestamp=" + timestamp + "&sign=" + sign;
            // }
            //
            // Map<String, Object> message = new HashMap<>();
            // message.put("msgtype", msgType);
            //
            // if ("text".equals(msgType)) {
            //     Map<String, Object> text = new HashMap<>();
            //     text.put("content", request.getContent());
            //     message.put("text", text);
            //
            //     Map<String, Object> at = new HashMap<>();
            //     at.put("atMobiles", request.getReceivers());
            //     at.put("isAtAll", atAll);
            //     message.put("at", at);
            // } else if ("markdown".equals(msgType)) {
            //     Map<String, Object> markdown = new HashMap<>();
            //     markdown.put("title", getConfigValue(request.getChannelConfig(), "title", "通知"));
            //     markdown.put("text", request.getContent());
            //     message.put("markdown", markdown);
            // }
            //
            // HttpClient client = HttpClient.newHttpClient();
            // HttpRequest httpRequest = HttpRequest.newBuilder()
            //     .uri(URI.create(webhookUrl))
            //     .header("Content-Type", "application/json")
            //     .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toJson(message)))
            //     .build();
            //
            // HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // 模拟发送
            boolean success = mockDingtalkWebhook(webhookUrl, msgType, request.getContent(), atAll);

            String receiver = "dingtalk-" + webhookUrl.hashCode();
            receiverStatus.put(receiver, success);

            if (success) {
                log.info(">>> [GOTONE-WEBHOOK-DINGTALK] 钉钉消息发送成功");
                return ChannelSendResponse.builder()
                        .content(request.getContent())
                        .successCount(1)
                        .failedCount(0)
                        .receiverStatus(receiverStatus)
                        .receiverErrors(receiverErrors)
                        .build();
            } else {
                receiverErrors.put(receiver, "发送失败");
                log.error(">>> [GOTONE-WEBHOOK-DINGTALK] 钉钉消息发送失败");
                return ChannelSendResponse.builder()
                        .content(request.getContent())
                        .successCount(0)
                        .failedCount(1)
                        .receiverStatus(receiverStatus)
                        .receiverErrors(receiverErrors)
                        .build();
            }

        } catch (Exception e) {
            log.error(">>> [GOTONE-WEBHOOK-DINGTALK] 发送钉钉消息异常", e);

            String receiver = "dingtalk-error";
            receiverStatus.put(receiver, false);
            receiverErrors.put(receiver, "系统异常: " + e.getMessage());

            return ChannelSendResponse.builder()
                    .content(request.getContent())
                    .successCount(0)
                    .failedCount(1)
                    .receiverStatus(receiverStatus)
                    .receiverErrors(receiverErrors)
                    .build();
        }
    }

    @Override
    public boolean isAvailable() {
        // Webhook 不需要全局配置，只需要在渠道配置中提供 webhookUrl 即可
        return true;
    }

    @Override
    public String getProviderName() {
        return "dingtalk";
    }

    /**
     * 模拟钉钉 Webhook 调用
     */
    @SuppressWarnings("unused")
    private boolean mockDingtalkWebhook(String webhookUrl, String msgType, String content, boolean atAll) {
        // TODO: 实际集成时替换为真实的 HTTP 调用
        log.debug(
                ">>> [GOTONE-WEBHOOK-DINGTALK] Mock Webhook Call: url={}, msgtype={}, content length={}",
                maskWebhookUrl(webhookUrl),
                msgType,
                content.length());

        // 模拟：92% 成功率
        return Math.random() > 0.08;
    }

    /**
     * Webhook URL 脱敏显示
     */
    private String maskWebhookUrl(String url) {
        if (url == null || url.length() < 50) {
            return "***";
        }
        return url.substring(0, 40) + "...";
    }

    /**
     * 从配置中获取值（带默认值）
     */
    private String getConfigValue(Map<String, Object> config, String key, String defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * 构建错误响应
     */
    private ChannelSendResponse buildErrorResponse(String errorMessage, int total, int success) {
        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();

        log.error(">>> [GOTONE-WEBHOOK-DINGTALK] {}", errorMessage);

        return ChannelSendResponse.builder()
                .content(null)
                .successCount(success)
                .failedCount(total - success)
                .receiverStatus(receiverStatus)
                .receiverErrors(receiverErrors)
                .build();
    }
}
