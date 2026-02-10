package io.github.loadup.components.gotone.channel.push;

/*-
 * #%L
 * loadup-components-gotone-channel-push
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
import org.springframework.beans.factory.annotation.Value;

/**
 * Firebase Cloud Messaging (FCM) 推送提供商
 *
 * <p>配置示例：
 * <pre>
 * loadup.gotone.push.fcm.server-key=YOUR_SERVER_KEY
 * loadup.gotone.push.fcm.project-id=YOUR_PROJECT_ID
 * </pre>
 */
@Slf4j
public class FcmPushProvider implements NotificationChannelProvider {

    @Value("${loadup.gotone.push.fcm.server-key:}")
    private String serverKey;

    @Value("${loadup.gotone.push.fcm.project-id:}")
    private String projectId;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        log.info(
                ">>> [GOTONE-PUSH-FCM] 开始发送推送, receivers={}, content length={}",
                request.getReceivers(),
                request.getContent() != null ? request.getContent().length() : 0);

        // 参数校验
        if (request.getReceivers() == null || request.getReceivers().isEmpty()) {
            return buildErrorResponse("收件人列表为空（设备Token）", 0, 0);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return buildErrorResponse("推送内容为空", request.getReceivers().size(), 0);
        }

        // 从渠道配置中获取推送标题、徽章等
        String title = getConfigValue(request.getChannelConfig(), "title", "通知");
        String sound = getConfigValue(request.getChannelConfig(), "sound", "default");
        Integer badge = Integer.parseInt(getConfigValue(request.getChannelConfig(), "badge", "1"));
        Map<String, Object> extras = getConfigMap(request.getChannelConfig(), "extras");

        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;

        try {
            // TODO: 实际集成 Firebase Cloud Messaging SDK
            // 示例代码：
            // FirebaseOptions options = FirebaseOptions.builder()
            //     .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
            //     .setProjectId(projectId)
            //     .build();
            // FirebaseApp.initializeApp(options);
            //
            // for (String deviceToken : request.getReceivers()) {
            //     Message message = Message.builder()
            //         .setToken(deviceToken)
            //         .setNotification(Notification.builder()
            //             .setTitle(title)
            //             .setBody(request.getContent())
            //             .build())
            //         .setApnsConfig(ApnsConfig.builder()
            //             .setAps(Aps.builder()
            //                 .setBadge(badge)
            //                 .setSound(sound)
            //                 .build())
            //             .build())
            //         .putAllData(extras)
            //         .build();
            //
            //     String messageId = FirebaseMessaging.getInstance().send(message);
            //     log.info(">>> [GOTONE-PUSH-FCM] 推送发送成功: messageId={}", messageId);
            // }

            // 模拟发送逻辑
            for (String deviceToken : request.getReceivers()) {
                if (!isValidDeviceToken(deviceToken)) {
                    receiverStatus.put(deviceToken, false);
                    receiverErrors.put(deviceToken, "无效的设备Token格式");
                    failedCount++;
                    log.warn(">>> [GOTONE-PUSH-FCM] 无效设备Token: {}", maskDeviceToken(deviceToken));
                    continue;
                }

                // 模拟调用 FCM API
                boolean sendSuccess = mockFcmApi(deviceToken, title, request.getContent(), sound, badge, extras);

                if (sendSuccess) {
                    receiverStatus.put(deviceToken, true);
                    successCount++;
                    log.info(">>> [GOTONE-PUSH-FCM] 推送发送成功: {}", maskDeviceToken(deviceToken));
                } else {
                    receiverStatus.put(deviceToken, false);
                    receiverErrors.put(deviceToken, "发送失败，请稍后重试");
                    failedCount++;
                    log.error(">>> [GOTONE-PUSH-FCM] 推送发送失败: {}", maskDeviceToken(deviceToken));
                }
            }

            return ChannelSendResponse.builder()
                    .content(request.getContent())
                    .successCount(successCount)
                    .failedCount(failedCount)
                    .receiverStatus(receiverStatus)
                    .receiverErrors(receiverErrors)
                    .build();

        } catch (Exception e) {
            log.error(">>> [GOTONE-PUSH-FCM] 发送推送异常", e);

            // 所有收件人标记为失败
            for (String receiver : request.getReceivers()) {
                receiverStatus.put(receiver, false);
                receiverErrors.put(receiver, "系统异常: " + e.getMessage());
            }

            return ChannelSendResponse.builder()
                    .content(request.getContent())
                    .successCount(0)
                    .failedCount(request.getReceivers().size())
                    .receiverStatus(receiverStatus)
                    .receiverErrors(receiverErrors)
                    .build();
        }
    }

    @Override
    public boolean isAvailable() {
        boolean available = serverKey != null && !serverKey.trim().isEmpty();

        if (!available) {
            log.warn(">>> [GOTONE-PUSH-FCM] Provider 不可用: Server Key 未配置");
        }

        return available;
    }

    @Override
    public String getProviderName() {
        return "fcm";
    }

    /**
     * 模拟 FCM API 调用
     */
    @SuppressWarnings("unused")
    private boolean mockFcmApi(
            String deviceToken, String title, String body, String sound, Integer badge, Map<String, Object> extras) {
        // TODO: 实际集成时替换为真实的 FCM SDK 调用
        log.debug(
                ">>> [GOTONE-PUSH-FCM] Mock API Call: token={}, title={}, body length={}",
                maskDeviceToken(deviceToken),
                title,
                body.length());

        // 模拟：88% 成功率
        return true;
    }

    /**
     * 验证设备Token格式（简单验证）
     */
    private boolean isValidDeviceToken(String deviceToken) {
        if (deviceToken == null || deviceToken.trim().isEmpty()) {
            return false;
        }

        // FCM Token 通常是 152 字符的字符串
        return deviceToken.length() >= 64 && deviceToken.length() <= 200;
    }

    /**
     * 设备Token脱敏显示
     */
    private String maskDeviceToken(String deviceToken) {
        if (deviceToken == null || deviceToken.length() < 10) {
            return "***";
        }
        return deviceToken.substring(0, 8) + "..." + deviceToken.substring(deviceToken.length() - 8);
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
     * 从配置中获取Map值
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getConfigMap(Map<String, Object> config, String key) {
        if (config == null || !config.containsKey(key)) {
            return new HashMap<>();
        }
        Object value = config.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return new HashMap<>();
    }

    /**
     * 构建错误响应
     */
    private ChannelSendResponse buildErrorResponse(String errorMessage, int total, int success) {
        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();

        log.error(">>> [GOTONE-PUSH-FCM] {}", errorMessage);

        return ChannelSendResponse.builder()
                .content(null)
                .successCount(success)
                .failedCount(total - success)
                .receiverStatus(receiverStatus)
                .receiverErrors(receiverErrors)
                .build();
    }
}
