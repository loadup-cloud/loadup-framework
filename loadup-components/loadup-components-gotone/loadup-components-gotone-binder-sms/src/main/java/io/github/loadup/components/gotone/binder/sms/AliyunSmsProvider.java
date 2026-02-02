package io.github.loadup.components.gotone.binder.sms;

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

import io.github.loadup.components.extension.annotation.Extension;
import io.github.loadup.components.gotone.api.INotificationProvider;
import io.github.loadup.components.gotone.enums.NotificationStatus;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 阿里云短信提供商实现 这是一个示例实现，实际使用需要集成阿里云 SDK */
@Slf4j
@Component
@Extension(bizCode = "SMS", useCase = "aliyun")
public class AliyunSmsProvider implements INotificationProvider {

    @Value("${loadup.gotone.sms.aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${loadup.gotone.sms.aliyun.access-key-secret:}")
    private String accessKeySecret;

    @Value("${loadup.gotone.sms.aliyun.sign-name:}")
    private String signName;

    @Override
    public NotificationResponse send(NotificationRequest request) {
        try {
            // TODO: 集成阿里云短信 SDK
            // DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId,
            // accessKeySecret);
            // IAcsClient client = new DefaultAcsClient(profile);
            // SendSmsRequest smsRequest = new SendSmsRequest();
            // ...

            log.info("Sending SMS via Aliyun to: {}", request.getReceivers());

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
            log.error("Failed to send SMS via Aliyun: {}", e.getMessage(), e);
            return buildErrorResponse(request, e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "aliyun";
    }

    @Override
    public boolean isAvailable() {
        return accessKeyId != null && !accessKeyId.isEmpty();
    }

    private void simulateSend(NotificationRequest request) {
        log.info("SMS Content: {}", request.getContent());
        log.info("SMS Receivers: {}", request.getReceivers());
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
