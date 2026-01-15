package com.github.loadup.components.gotone.binder.sms;

/*-
 * #%L
 * loadup-components-gotone-binder-sms
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

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.gotone.api.INotificationProvider;
import com.github.loadup.components.gotone.enums.NotificationStatus;
import com.github.loadup.components.gotone.model.NotificationRequest;
import com.github.loadup.components.gotone.model.NotificationResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 华为云短信提供商实现
 * 这是一个示例实现，实际使用需要集成华为云 SDK
 */
@Slf4j
@Component
@Extension(bizCode = "SMS", useCase = "huawei")
public class HuaweiSmsProvider implements INotificationProvider {

    @Value("${loadup.gotone.sms.huawei.app-key:}")
    private String appKey;

    @Value("${loadup.gotone.sms.huawei.app-secret:}")
    private String appSecret;

    @Value("${loadup.gotone.sms.huawei.sender:}")
    private String sender;

    @Value("${loadup.gotone.sms.huawei.signature:}")
    private String signature;

    @Override
    public NotificationResponse send(NotificationRequest request) {
        try {
            // TODO: 集成华为云短信 SDK
            // SmsClient client = new SmsClient(appKey, appSecret);
            // SendSmsRequest smsRequest = new SendSmsRequest();
            // ...

            log.info("Sending SMS via Huawei to: {}", request.getReceivers());

            // 模拟发送
            simulateSend(request);

            return NotificationResponse.builder()
                .success(true)
                .status(NotificationStatus.SUCCESS)
                .bizId(request.getBizId())
                .messageId(UUID.randomUUID().toString())
                .provider(getProviderName())
                .sendTime(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            log.error("Failed to send SMS via Huawei: {}", e.getMessage(), e);
            return buildErrorResponse(request, e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "huawei";
    }

    @Override
    public boolean isAvailable() {
        return appKey != null && !appKey.isEmpty()
            && appSecret != null && !appSecret.isEmpty();
    }

    private void simulateSend(NotificationRequest request) {
        log.info("SMS Content: {}", request.getContent());
        log.info("SMS Receivers: {}", request.getReceivers());
        log.info("Sender: {}, Signature: {}", sender, signature);
    }

    private NotificationResponse buildErrorResponse(NotificationRequest request, String errorMessage) {
        return NotificationResponse.builder()
            .success(false)
            .status(NotificationStatus.FAILED)
            .bizId(request.getBizId())
            .provider(getProviderName())
            .errorMessage(errorMessage)
            .sendTime(LocalDateTime.now())
            .build();
    }
}

