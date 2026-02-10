package io.github.loadup.components.gotone.channel.sms;

/*-
 * #%L
 * loadup-components-gotone-channel-sms
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
 * 云片短信渠道提供商
 *
 * <p>配置示例：
 * <pre>
 * loadup.gotone.sms.yunpian.api-key=YOUR_API_KEY
 * loadup.gotone.sms.yunpian.api-url=https://sms.yunpian.com/v2/sms/single_send.json
 * </pre>
 */
@Slf4j
public class YunpianSmsProvider implements NotificationChannelProvider {

    @Value("${loadup.gotone.sms.yunpian.api-key:}")
    private String apiKey;

    @Value("${loadup.gotone.sms.yunpian.api-url:https://sms.yunpian.com/v2/sms/single_send.json}")
    private String apiUrl;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        log.info(
                ">>> [GOTONE-SMS-YUNPIAN] 开始发送短信, receivers={}, content length={}",
                request.getReceivers(),
                request.getContent() != null ? request.getContent().length() : 0);

        // 参数校验
        if (request.getReceivers() == null || request.getReceivers().isEmpty()) {
            return buildErrorResponse("收件人列表为空", 0, 0);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return buildErrorResponse("短信内容为空", request.getReceivers().size(), 0);
        }

        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;

        try {
            // TODO: 实际集成云片 SMS API
            // 示例代码：
            // HttpClient client = HttpClient.newHttpClient();
            // for (String phoneNumber : request.getReceivers()) {
            //     Map<String, String> params = new HashMap<>();
            //     params.put("apikey", apiKey);
            //     params.put("mobile", phoneNumber);
            //     params.put("text", request.getContent());
            //
            //     HttpRequest httpRequest = HttpRequest.newBuilder()
            //         .uri(URI.create(apiUrl))
            //         .header("Content-Type", "application/x-www-form-urlencoded")
            //         .POST(HttpRequest.BodyPublishers.ofString(buildFormData(params)))
            //         .build();
            //
            //     HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //     // 解析响应...
            // }

            // 模拟发送逻辑
            for (String phoneNumber : request.getReceivers()) {
                if (!isValidPhoneNumber(phoneNumber)) {
                    receiverStatus.put(phoneNumber, false);
                    receiverErrors.put(phoneNumber, "无效的手机号码格式");
                    failedCount++;
                    log.warn(">>> [GOTONE-SMS-YUNPIAN] 无效手机号: {}", phoneNumber);
                    continue;
                }

                // 模拟调用云片API
                boolean sendSuccess = mockYunpianSmsApi(phoneNumber, request);

                if (sendSuccess) {
                    receiverStatus.put(phoneNumber, true);
                    successCount++;
                    log.info(">>> [GOTONE-SMS-YUNPIAN] 短信发送成功: {}", maskPhoneNumber(phoneNumber));
                } else {
                    receiverStatus.put(phoneNumber, false);
                    receiverErrors.put(phoneNumber, "发送失败，请稍后重试");
                    failedCount++;
                    log.error(">>> [GOTONE-SMS-YUNPIAN] 短信发送失败: {}", maskPhoneNumber(phoneNumber));
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
            log.error(">>> [GOTONE-SMS-YUNPIAN] 发送短信异常", e);

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
        boolean available = apiKey != null && !apiKey.trim().isEmpty();

        if (!available) {
            log.warn(">>> [GOTONE-SMS-YUNPIAN] Provider 不可用: API Key 未配置");
        }

        return available;
    }

    @Override
    public String getProviderName() {
        return "yunpian";
    }

    /**
     * 模拟云片短信API调用
     *
     * @param phoneNumber 手机号
     * @param request 请求
     * @return 是否成功
     */
    @SuppressWarnings("unused")
    private boolean mockYunpianSmsApi(String phoneNumber, ChannelSendRequest request) {
        // TODO: 实际集成时替换为真实的云片API调用
        log.debug(
                ">>> [GOTONE-SMS-YUNPIAN] Mock API Call: phone={}, content length={}",
                maskPhoneNumber(phoneNumber),
                request.getContent().length());

        // 模拟：85% 成功率
        return Math.random() > 0.15;
    }

    /**
     * 验证手机号码格式（简单验证）
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // 中国手机号：1开头，11位数字
        // 国际号码：支持+开头
        String cleaned = phoneNumber.replaceAll("[\\s-]", "");
        return cleaned.matches("^(\\+?86)?1[3-9]\\d{9}$") || cleaned.matches("^\\+\\d{1,3}\\d{6,14}$");
    }

    /**
     * 手机号脱敏显示
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 7) {
            return "***";
        }
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    /**
     * 构建错误响应
     */
    private ChannelSendResponse buildErrorResponse(String errorMessage, int total, int success) {
        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();

        log.error(">>> [GOTONE-SMS-YUNPIAN] {}", errorMessage);

        return ChannelSendResponse.builder()
                .content(null)
                .successCount(success)
                .failedCount(total - success)
                .receiverStatus(receiverStatus)
                .receiverErrors(receiverErrors)
                .build();
    }
}
