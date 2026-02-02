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

/** 腾讯云短信提供商实现 这是一个示例实现，实际使用需要集成腾讯云 SDK */
@Slf4j
@Component
@Extension(bizCode = "SMS", useCase = "tencent")
public class TencentSmsProvider implements INotificationProvider {

  @Value("${loadup.gotone.sms.tencent.secret-id:}")
  private String secretId;

  @Value("${loadup.gotone.sms.tencent.secret-key:}")
  private String secretKey;

  @Value("${loadup.gotone.sms.tencent.sdk-app-id:}")
  private String sdkAppId;

  @Value("${loadup.gotone.sms.tencent.sign-name:}")
  private String signName;

  @Override
  public NotificationResponse send(NotificationRequest request) {
    try {
      // TODO: 集成腾讯云短信 SDK
      // Credential cred = new Credential(secretId, secretKey);
      // SmsClient client = new SmsClient(cred, "ap-guangzhou");
      // SendSmsRequest req = new SendSmsRequest();
      // ...

      log.info("Sending SMS via Tencent to: {}", request.getReceivers());

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
      log.error("Failed to send SMS via Tencent: {}", e.getMessage(), e);
      return buildErrorResponse(request, e.getMessage());
    }
  }

  @Override
  public String getProviderName() {
    return "tencent";
  }

  @Override
  public boolean isAvailable() {
    return secretId != null && !secretId.isEmpty() && secretKey != null && !secretKey.isEmpty();
  }

  private void simulateSend(NotificationRequest request) {
    log.info("SMS Content: {}", request.getContent());
    log.info("SMS Receivers: {}", request.getReceivers());
    log.info("SDK App ID: {}", sdkAppId);
  }

  private NotificationResponse buildErrorResponse(
      NotificationRequest request, String errorMessage) {
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
