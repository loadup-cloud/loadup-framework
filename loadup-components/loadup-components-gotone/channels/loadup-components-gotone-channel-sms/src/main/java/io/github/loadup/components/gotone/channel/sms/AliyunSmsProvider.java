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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云短信渠道提供商
 *
 * <p>配置示例：
 * <pre>
 * loadup.gotone.sms.aliyun.access-key-id=YOUR_ACCESS_KEY_ID
 * loadup.gotone.sms.aliyun.access-key-secret=YOUR_ACCESS_KEY_SECRET
 * loadup.gotone.sms.aliyun.sign-name=YOUR_SIGN_NAME
 * loadup.gotone.sms.aliyun.region-id=cn-hangzhou
 * </pre>
 */
@Slf4j
public class AliyunSmsProvider implements NotificationChannelProvider {

    @Value("${loadup.gotone.sms.aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${loadup.gotone.sms.aliyun.access-key-secret:}")
    private String accessKeySecret;

    @Value("${loadup.gotone.sms.aliyun.sign-name:}")
    private String signName;

    @Value("${loadup.gotone.sms.aliyun.region-id:cn-hangzhou}")
    private String regionId;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        log.info(">>> [GOTONE-SMS-ALIYUN] 开始发送短信, receivers={}, content length={}",
                request.getReceivers(), request.getContent() != null ? request.getContent().length() : 0);

        // 参数校验
        if (request.getReceivers() == null || request.getReceivers().isEmpty()) {
            return buildErrorResponse("收件人列表为空", 0, 0);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return buildErrorResponse("短信内容为空", request.getReceivers().size(), 0);
        }

        // 从渠道配置中获取短信模板ID和签名（优先级高于全局配置）
        String templateCode = getConfigValue(request.getChannelConfig(), "templateId", null);
        String currentSignName = getConfigValue(request.getChannelConfig(), "signName", signName);

        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;

        try {
            // TODO: 实际集成阿里云 SMS SDK
            // 示例代码：
            // DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
            // IAcsClient client = new DefaultAcsClient(profile);
            // SendSmsRequest smsRequest = new SendSmsRequest();
            // smsRequest.setPhoneNumbers(String.join(",", request.getReceivers()));
            // smsRequest.setSignName(currentSignName);
            // smsRequest.setTemplateCode(templateCode);
            // smsRequest.setTemplateParam(JsonUtil.toJson(request.getTemplateParams()));
            // SendSmsResponse response = client.getAcsResponse(smsRequest);

            // 模拟发送逻辑
            for (String phoneNumber : request.getReceivers()) {
                if (!isValidPhoneNumber(phoneNumber)) {
                    receiverStatus.put(phoneNumber, false);
                    receiverErrors.put(phoneNumber, "无效的手机号码格式");
                    failedCount++;
                    log.warn(">>> [GOTONE-SMS-ALIYUN] 无效手机号: {}", phoneNumber);
                    continue;
                }

                // 模拟调用阿里云API
                boolean sendSuccess = mockAliyunSmsApi(phoneNumber, currentSignName, templateCode, request);

                if (sendSuccess) {
                    receiverStatus.put(phoneNumber, true);
                    successCount++;
                    log.info(">>> [GOTONE-SMS-ALIYUN] 短信发送成功: {}", maskPhoneNumber(phoneNumber));
                } else {
                    receiverStatus.put(phoneNumber, false);
                    receiverErrors.put(phoneNumber, "发送失败，请稍后重试");
                    failedCount++;
                    log.error(">>> [GOTONE-SMS-ALIYUN] 短信发送失败: {}", maskPhoneNumber(phoneNumber));
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
            log.error(">>> [GOTONE-SMS-ALIYUN] 发送短信异常", e);

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
        boolean available = accessKeyId != null && !accessKeyId.trim().isEmpty()
                && accessKeySecret != null && !accessKeySecret.trim().isEmpty();

        if (!available) {
            log.warn(">>> [GOTONE-SMS-ALIYUN] Provider 不可用: AccessKey 未配置");
        }

        return available;
    }

    @Override
    public String getProviderName() {
        return "aliyun";
    }

    /**
     * 模拟阿里云短信API调用
     *
     * @param phoneNumber 手机号
     * @param signName 签名
     * @param templateCode 模板ID
     * @param request 请求
     * @return 是否成功
     */
    @SuppressWarnings("unused")
    private boolean mockAliyunSmsApi(String phoneNumber, String signName, String templateCode, ChannelSendRequest request) {
        // TODO: 实际集成时替换为真实的阿里云SDK调用
        log.debug(">>> [GOTONE-SMS-ALIYUN] Mock API Call: phone={}, sign={}, template={}",
                maskPhoneNumber(phoneNumber), signName, templateCode);

        // 模拟：90% 成功率
        return Math.random() > 0.1;
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

        log.error(">>> [GOTONE-SMS-ALIYUN] {}", errorMessage);

        return ChannelSendResponse.builder()
                .content(null)
                .successCount(success)
                .failedCount(total - success)
                .receiverStatus(receiverStatus)
                .receiverErrors(receiverErrors)
                .build();
    }
}
