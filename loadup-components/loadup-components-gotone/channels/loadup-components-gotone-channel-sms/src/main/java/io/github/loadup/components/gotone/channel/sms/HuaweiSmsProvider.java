package io.github.loadup.components.gotone.channel.sms;

/*-
 * #%L
 * loadup-components-gotone-channel-sms
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
 * 华为云短信渠道提供商
 *
 * <p>配置示例：
 * <pre>
 * loadup.gotone.sms.huawei.app-key=YOUR_APP_KEY
 * loadup.gotone.sms.huawei.app-secret=YOUR_APP_SECRET
 * loadup.gotone.sms.huawei.sender=YOUR_SENDER
 * loadup.gotone.sms.huawei.signature=YOUR_SIGNATURE
 * loadup.gotone.sms.huawei.endpoint=https://smsapi.cn-north-4.myhuaweicloud.com:443
 * </pre>
 */
public class HuaweiSmsProvider implements GotoneProvider {
    private static final Logger log = LoggerFactory.getLogger(HuaweiSmsProvider.class);

    private final String appKey;

    private final String appSecret;

    private final String sender;

    private final String signature;

    private final String endpoint;

    @Override
    public String getChannelType() {
        return "SMS";
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        log.info(
                ">>> [GOTONE-SMS-HUAWEI] 开始发送短信, receivers={}, content length={}",
                request.getReceivers(),
                request.getContent() != null ? request.getContent().length() : 0);

        // 参数校验
        if (request.getReceivers() == null || request.getReceivers().isEmpty()) {
            return buildErrorResponse("收件人列表为空", 0, 0);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return buildErrorResponse("短信内容为空", request.getReceivers().size(), 0);
        }

        // 从渠道配置中获取短信模板ID和签名（优先级高于全局配置）
        String templateId = getConfigValue(request.getChannelConfig(), "templateId", null);
        String currentSignature = getConfigValue(request.getChannelConfig(), "signature", signature);
        String currentSender = getConfigValue(request.getChannelConfig(), "sender", sender);

        Map<String, Boolean> receiverStatus = new HashMap<>();
        Map<String, String> receiverErrors = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;

        try {
            // TODO: 实际集成华为云 SMS SDK
            // 示例代码：
            // SmsClient client = SmsClient.newBuilder()
            //     .withCredential(auth)
            //     .withEndpoint(endpoint)
            //     .build();
            //
            // SendSmsRequest request = new SendSmsRequest();
            // request.withBody(new SendSmsRequestBody()
            //     .withFrom(currentSender)
            //     .withTo(request.getReceivers())
            //     .withTemplateId(templateId)
            //     .withTemplateParas(JsonUtil.toJson(request.getTemplateParams()))
            //     .withSignature(currentSignature));
            //
            // SendSmsResponse response = client.sendSms(request);

            // 模拟发送逻辑
            for (String phoneNumber : request.getReceivers()) {
                if (!isValidPhoneNumber(phoneNumber)) {
                    receiverStatus.put(phoneNumber, false);
                    receiverErrors.put(phoneNumber, "无效的手机号码格式");
                    failedCount++;
                    log.warn(">>> [GOTONE-SMS-HUAWEI] 无效手机号: {}", phoneNumber);
                    continue;
                }

                // 模拟调用华为云API
                boolean sendSuccess =
                        mockHuaweiSmsApi(phoneNumber, currentSender, currentSignature, templateId, request);

                if (sendSuccess) {
                    receiverStatus.put(phoneNumber, true);
                    successCount++;
                    log.info(">>> [GOTONE-SMS-HUAWEI] 短信发送成功: {}", maskPhoneNumber(phoneNumber));
                } else {
                    receiverStatus.put(phoneNumber, false);
                    receiverErrors.put(phoneNumber, "发送失败，请稍后重试");
                    failedCount++;
                    log.error(">>> [GOTONE-SMS-HUAWEI] 短信发送失败: {}", maskPhoneNumber(phoneNumber));
                }
            }

            ChannelSendResponse sendResponse = new ChannelSendResponse();
            sendResponse.setContent(request.getContent());
            sendResponse.setSuccessCount(successCount);
            sendResponse.setFailedCount(failedCount);
            sendResponse.setReceiverStatus(receiverStatus);
            sendResponse.setReceiverErrors(receiverErrors);
            return sendResponse;

        } catch (Exception e) {
            log.error(">>> [GOTONE-SMS-HUAWEI] 发送短信异常", e);

            // 所有收件人标记为失败
            for (String receiver : request.getReceivers()) {
                receiverStatus.put(receiver, false);
                receiverErrors.put(receiver, "系统异常: " + e.getMessage());
            }

            ChannelSendResponse sendResponse = new ChannelSendResponse();
            sendResponse.setContent(request.getContent());
            sendResponse.setSuccessCount(0);
            sendResponse.setFailedCount(request.getReceivers().size());
            sendResponse.setReceiverStatus(receiverStatus);
            sendResponse.setReceiverErrors(receiverErrors);
            return sendResponse;
        }
    }

    @Override
    public boolean isAvailable() {
        boolean available = appKey != null
                && !appKey.trim().isEmpty()
                && appSecret != null
                && !appSecret.trim().isEmpty()
                && sender != null
                && !sender.trim().isEmpty();

        if (!available) {
            log.warn(">>> [GOTONE-SMS-HUAWEI] Provider 不可用: AppKey/AppSecret/Sender 未配置");
        }

        return available;
    }

    @Override
    public String getProviderName() {
        return "huawei";
    }

    /**
     * 模拟华为云短信API调用
     *
     * @param phoneNumber 手机号
     * @param sender      发送方
     * @param signature   签名
     * @param templateId  模板ID
     * @param request     请求
     * @return 是否成功
     */
    @SuppressWarnings("unused")
    private boolean mockHuaweiSmsApi(
            String phoneNumber, String sender, String signature, String templateId, ChannelSendRequest request) {
        // TODO: 实际集成时替换为真实的华为云SDK调用
        log.debug(
                ">>> [GOTONE-SMS-HUAWEI] Mock API Call: phone={}, sender={}, signature={}, template={}",
                maskPhoneNumber(phoneNumber),
                sender,
                signature,
                templateId);

        // 模拟：87% 成功率
        return ThreadLocalRandom.current().nextDouble() > 0.13;
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

        log.error(">>> [GOTONE-SMS-HUAWEI] {}", errorMessage);

        ChannelSendResponse sendResponse = new ChannelSendResponse();
        sendResponse.setContent(null);
        sendResponse.setSuccessCount(success);
        sendResponse.setFailedCount(total - success);
        sendResponse.setReceiverStatus(receiverStatus);
        sendResponse.setReceiverErrors(receiverErrors);
        return sendResponse;
    }

    public HuaweiSmsProvider(String appKey, String appSecret, String sender, String signature, String endpoint) {
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.sender = sender;
        this.signature = signature;
        this.endpoint = endpoint;
    }
}
