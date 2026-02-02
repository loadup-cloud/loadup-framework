package io.github.loadup.components.gotone.binder.push;

/*-
 * #%L
 * loadup-components-gotone-binder-push
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

/** Firebase Cloud Messaging (FCM) 推送提供商 这是一个示例实现，实际使用需要集成 Firebase SDK */
@Slf4j
@Component
@Extension(bizCode = "PUSH", useCase = "fcm")
public class FcmPushProvider implements INotificationProvider {

  @Value("${loadup.gotone.push.fcm.server-key:}")
  private String serverKey;

  @Override
  public NotificationResponse send(NotificationRequest request) {
    try {
      // TODO: 集成 Firebase Cloud Messaging SDK
      // FirebaseOptions options = FirebaseOptions.builder()
      //     .setCredentials(GoogleCredentials.fromStream(...))
      //     .build();
      // FirebaseApp.initializeApp(options);

      log.info("Sending push notification via FCM to: {}", request.getReceivers());

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
      log.error("Failed to send push notification via FCM: {}", e.getMessage(), e);
      return buildErrorResponse(request, e.getMessage());
    }
  }

  @Override
  public String getProviderName() {
    return "fcm";
  }

  @Override
  public boolean isAvailable() {
    return serverKey != null && !serverKey.isEmpty();
  }

  private void simulateSend(NotificationRequest request) {
    log.info("Push Title: {}", request.getTitle());
    log.info("Push Content: {}", request.getContent());
    log.info("Push Receivers: {}", request.getReceivers());
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
