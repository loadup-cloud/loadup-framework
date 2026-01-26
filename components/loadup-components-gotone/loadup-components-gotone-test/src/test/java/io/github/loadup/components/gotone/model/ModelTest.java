package io.github.loadup.components.gotone.model;

/*-
 * #%L
 * loadup-components-gotone-test
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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.gotone.enums.NotificationChannel;
import io.github.loadup.components.gotone.enums.NotificationStatus;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;

/**
 * Model 类测试
 */
class ModelTest {

    @Test
    void testNotificationRequestBuilder() {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value1");

        // When
        NotificationRequest request = NotificationRequest.builder()
            .bizId("test-001")
            .channel(NotificationChannel.SMS)
            .receivers(List.of("13800138000"))
            .templateCode("TEST_TEMPLATE")
            .title("Test Title")
            .content("Test Content")
            .templateParams(params)
            .priority(10)
            .async(true)
            .providers(List.of("aliyun"))
            .build();

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getBizId()).isEqualTo("test-001");
        assertThat(request.getChannel()).isEqualTo(NotificationChannel.SMS);
        assertThat(request.getReceivers()).containsExactly("13800138000");
        assertThat(request.getTemplateCode()).isEqualTo("TEST_TEMPLATE");
        assertThat(request.getTitle()).isEqualTo("Test Title");
        assertThat(request.getContent()).isEqualTo("Test Content");
        assertThat(request.getTemplateParams()).containsEntry("key1", "value1");
        assertThat(request.getPriority()).isEqualTo(10);
        assertThat(request.getAsync()).isTrue();
        assertThat(request.getProviders()).containsExactly("aliyun");
    }

    @Test
    void testNotificationRequestSetters() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .bizId("biz-001")
            .channel(NotificationChannel.EMAIL)
            .receivers(List.of("user@example.com"))
            .templateCode("EMAIL_TEMPLATE")
            .title("Email Title")
            .content("Email Content")
            .priority(5)
            .build();

        // Then
        assertThat(request.getBizId()).isEqualTo("biz-001");
        assertThat(request.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(request.getReceivers()).hasSize(1);
        assertThat(request.getTitle()).isEqualTo("Email Title");
        assertThat(request.getTemplateCode()).isEqualTo("EMAIL_TEMPLATE");
        assertThat(request.getContent()).isEqualTo("Email Content");
        assertThat(request.getPriority()).isEqualTo(5);
    }

    @Test
    void testNotificationResponseBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        NotificationResponse response = NotificationResponse.builder()
            .success(true)
            .status(NotificationStatus.SUCCESS)
            .bizId("test-001")
            .messageId("msg-001")
            .provider("aliyun")
            .errorMessage(null)
            .sendTime(now)
            .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(response.getBizId()).isEqualTo("test-001");
        assertThat(response.getMessageId()).isEqualTo("msg-001");
        assertThat(response.getProvider()).isEqualTo("aliyun");
        assertThat(response.getErrorMessage()).isNull();
        assertThat(response.getSendTime()).isEqualTo(now);
    }

    @Test
    void testNotificationResponseSetters() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        NotificationResponse response = NotificationResponse.builder()
            .success(false)
            .status(NotificationStatus.FAILED)
            .bizId("biz-002")
            .messageId("msg-002")
            .provider("tencent")
            .errorMessage("Connection timeout")
            .sendTime(now)
            .build();

        // Then
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(response.getBizId()).isEqualTo("biz-002");
        assertThat(response.getMessageId()).isEqualTo("msg-002");
        assertThat(response.getProvider()).isEqualTo("tencent");
        assertThat(response.getErrorMessage()).isEqualTo("Connection timeout");
        assertThat(response.getSendTime()).isEqualTo(now);
    }

    @Test
    void testNotificationRequestWithNullValues() {
        // When
        NotificationRequest request = NotificationRequest.builder()
            .bizId(null)
            .channel(NotificationChannel.SMS)
            .receivers(null)
            .templateParams(null)
            .build();

        // Then
        assertThat(request.getBizId()).isNull();
        assertThat(request.getReceivers()).isNull();
        assertThat(request.getTemplateParams()).isNull();
    }

    @Test
    void testNotificationResponseWithNullValues() {
        // When
        NotificationResponse response = NotificationResponse.builder()
            .success(false)
            .status(NotificationStatus.FAILED)
            .messageId(null)
            .errorMessage(null)
            .sendTime(null)
            .build();

        // Then
        assertThat(response.getMessageId()).isNull();
        assertThat(response.getErrorMessage()).isNull();
        assertThat(response.getSendTime()).isNull();
    }

    @Test
    void testNotificationRequestMultipleReceivers() {
        // When
        NotificationRequest request = NotificationRequest.builder()
            .receivers(Arrays.asList("user1@example.com", "user2@example.com", "user3@example.com"))
            .build();

        // Then
        assertThat(request.getReceivers()).hasSize(3);
        assertThat(request.getReceivers()).contains("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    void testNotificationRequestTemplateParams() {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("userName", "张三");
        params.put("orderId", "202512300001");
        params.put("amount", 99.99);

        // When
        NotificationRequest request = NotificationRequest.builder()
            .templateParams(params)
            .build();

        // Then
        assertThat(request.getTemplateParams()).hasSize(3);
        assertThat(request.getTemplateParams().get("userName")).isEqualTo("张三");
        assertThat(request.getTemplateParams().get("orderId")).isEqualTo("202512300001");
        assertThat(request.getTemplateParams().get("amount")).isEqualTo(99.99);
    }

    @Test
    void testNotificationChannelEnum() {
        // Test all enum values
        assertThat(NotificationChannel.SMS).isNotNull();
        assertThat(NotificationChannel.EMAIL).isNotNull();
        assertThat(NotificationChannel.PUSH).isNotNull();

        // Test valueOf
        assertThat(NotificationChannel.valueOf("SMS")).isEqualTo(NotificationChannel.SMS);
        assertThat(NotificationChannel.valueOf("EMAIL")).isEqualTo(NotificationChannel.EMAIL);
        assertThat(NotificationChannel.valueOf("PUSH")).isEqualTo(NotificationChannel.PUSH);
    }

    @Test
    void testNotificationStatusEnum() {
        // Test all enum values
        assertThat(NotificationStatus.SUCCESS).isNotNull();
        assertThat(NotificationStatus.FAILED).isNotNull();
        assertThat(NotificationStatus.PENDING).isNotNull();

        // Test valueOf
        assertThat(NotificationStatus.valueOf("SUCCESS")).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(NotificationStatus.valueOf("FAILED")).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void testNotificationRequestToString() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .bizId("test-001")
            .channel(NotificationChannel.SMS)
            .build();

        // When
        String result = request.toString();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("test-001");
    }

    @Test
    void testNotificationResponseToString() {
        // Given
        NotificationResponse response = NotificationResponse.builder()
            .success(true)
            .bizId("test-001")
            .build();

        // When
        String result = response.toString();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("test-001");
    }
}

