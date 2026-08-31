package io.github.loadup.components.gotone.channel.webhook.provider;

/*-
 * #%L
 * loadup-components-gotone-channel-webhook
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 企业微信机器人 Webhook 提供商
 *
 * <p>配置示例：
 * <pre>
 * # 渠道配置（channelConfig）中需要包含：
 * {
 *   "webhookUrl": "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx",
 *   "msgtype": "text",        # text/markdown/image/news
 *   "mentionedList": ["@all"], # @所有人 或 指定成员UserID
 *   "mentionedMobileList": ["13800138000"]  # @手机号
 * }
 * </pre>
 *
 * @see <a href="https://developer.work.weixin.qq.com/document/path/91770">企业微信群机器人配置说明</a>
 */
public class WechatWebhookProvider implements GotoneProvider {
    private static final Logger log = LoggerFactory.getLogger(WechatWebhookProvider.class);

    @Override
    public String getChannelType() {
        return "WEBHOOK";
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        log.info(
                ">>> [GOTONE-WEBHOOK-WECHAT] 开始发送企业微信消息, receivers={}, content length={}",
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
        String msgType = getConfigValue(request.getChannelConfig(), "msgtype", "text");

        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();

        try {
            // TODO: 实际集成企业微信 Webhook API
            // 示例代码：
            // Map<String, Object> message = new HashMap<>();
            // message.put("msgtype", msgType);
            //
            // if ("text".equals(msgType)) {
            //     Map<String, Object> text = new HashMap<>();
            //     text.put("content", request.getContent());
            //     text.put("mentioned_list", getConfigList(request.getChannelConfig(), "mentionedList"));
            //     text.put("mentioned_mobile_list", getConfigList(request.getChannelConfig(), "mentionedMobileList"));
            //     message.put("text", text);
            // } else if ("markdown".equals(msgType)) {
            //     Map<String, Object> markdown = new HashMap<>();
            //     markdown.put("content", request.getContent());
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
            boolean success = mockWechatWebhook(webhookUrl, msgType, request.getContent());

            String receiver = "wechat-" + webhookUrl.hashCode();
            receiverStatus.put(receiver, success);

            if (success) {
                log.info(">>> [GOTONE-WEBHOOK-WECHAT] 企业微信消息发送成功");
                ChannelSendResponse sendResponse = new ChannelSendResponse();
                sendResponse.setContent(request.getContent());
                sendResponse.setSuccessCount(1);
                sendResponse.setFailedCount(0);
                sendResponse.setReceiverStatus(receiverStatus);
                sendResponse.setReceiverErrors(receiverErrors);
                return sendResponse;
            } else {
                receiverErrors.put(receiver, "发送失败");
                log.error(">>> [GOTONE-WEBHOOK-WECHAT] 企业微信消息发送失败");
                ChannelSendResponse sendResponse = new ChannelSendResponse();
                sendResponse.setContent(request.getContent());
                sendResponse.setSuccessCount(0);
                sendResponse.setFailedCount(1);
                sendResponse.setReceiverStatus(receiverStatus);
                sendResponse.setReceiverErrors(receiverErrors);
                return sendResponse;
            }

        } catch (Exception e) {
            log.error(">>> [GOTONE-WEBHOOK-WECHAT] 发送企业微信消息异常", e);

            String receiver = "wechat-error";
            receiverStatus.put(receiver, false);
            receiverErrors.put(receiver, "系统异常: " + e.getMessage());

            ChannelSendResponse sendResponse = new ChannelSendResponse();
            sendResponse.setContent(request.getContent());
            sendResponse.setSuccessCount(0);
            sendResponse.setFailedCount(1);
            sendResponse.setReceiverStatus(receiverStatus);
            sendResponse.setReceiverErrors(receiverErrors);
            return sendResponse;
        }
    }

    @Override
    public boolean isAvailable() {
        // Webhook 不需要全局配置，只需要在渠道配置中提供 webhookUrl 即可
        return true;
    }

    @Override
    public String getProviderName() {
        return "wechat";
    }

    /**
     * 模拟企业微信 Webhook 调用
     */
    @SuppressWarnings("unused")
    private boolean mockWechatWebhook(String webhookUrl, String msgType, String content) {
        // TODO: 实际集成时替换为真实的 HTTP 调用
        log.debug(
                ">>> [GOTONE-WEBHOOK-WECHAT] Mock Webhook Call: url={}, msgtype={}, content length={}",
                maskWebhookUrl(webhookUrl),
                msgType,
                content.length());

        // 模拟：93% 成功率
        return ThreadLocalRandom.current().nextDouble() > 0.07;
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

        log.error(">>> [GOTONE-WEBHOOK-WECHAT] {}", errorMessage);

        ChannelSendResponse sendResponse = new ChannelSendResponse();
        sendResponse.setContent(null);
        sendResponse.setSuccessCount(success);
        sendResponse.setFailedCount(total - success);
        sendResponse.setReceiverStatus(receiverStatus);
        sendResponse.setReceiverErrors(receiverErrors);
        return sendResponse;
    }
}
