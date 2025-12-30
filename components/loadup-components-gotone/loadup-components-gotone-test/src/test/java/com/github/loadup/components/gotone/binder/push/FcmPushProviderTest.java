package com.github.loadup.components.gotone.binder.push;

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

import com.github.loadup.components.gotone.enums.NotificationChannel;
import com.github.loadup.components.gotone.enums.NotificationStatus;
import com.github.loadup.components.gotone.model.NotificationRequest;
import com.github.loadup.components.gotone.model.NotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FcmPushProvider 测试类
 */
class FcmPushProviderTest {

    private FcmPushProvider provider;

    @BeforeEach
    void setUp() {
        provider = new FcmPushProvider();
        ReflectionTestUtils.setField(provider, "serverKey", "test-server-key");
    }

    @Test
    void testSendSuccess() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .bizId("push-001")
            .channel(NotificationChannel.PUSH)
            .receivers(Arrays.asList("device-token-123"))
            .title("推送标题")
            .content("推送内容")
            .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(response.getBizId()).isEqualTo("push-001");
        assertThat(response.getProvider()).isEqualTo("fcm");
        assertThat(response.getMessageId()).isNotNull();
        assertThat(response.getSendTime()).isNotNull();
    }

    @Test
    void testSendWithMultipleDevices() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .bizId("push-002")
            .channel(NotificationChannel.PUSH)
            .receivers(Arrays.asList(
                "device-token-1",
                "device-token-2",
                "device-token-3"
            ))
            .title("批量推送")
            .content("批量推送测试")
            .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getProvider()).isEqualTo("fcm");
    }

    @Test
    void testGetProviderName() {
        assertThat(provider.getProviderName()).isEqualTo("fcm");
    }

    @Test
    void testIsAvailableWithValidConfig() {
        assertThat(provider.isAvailable()).isTrue();
    }

    @Test
    void testIsAvailableWithNullServerKey() {
        ReflectionTestUtils.setField(provider, "serverKey", null);
        assertThat(provider.isAvailable()).isFalse();
    }

    @Test
    void testIsAvailableWithEmptyServerKey() {
        ReflectionTestUtils.setField(provider, "serverKey", "");
        assertThat(provider.isAvailable()).isFalse();
    }

    @Test
    void testSendWithNullReceivers() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .bizId("push-003")
            .channel(NotificationChannel.PUSH)
            .receivers(null)
            .title("测试")
            .content("测试内容")
            .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
    }

    @Test
    void testSendWithEmptyTitle() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .bizId("push-004")
            .channel(NotificationChannel.PUSH)
            .receivers(Arrays.asList("device-token-123"))
            .title("")
            .content("只有内容")
            .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
    }

    @Test
    void testSendWithNullContent() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .bizId("push-005")
            .channel(NotificationChannel.PUSH)
            .receivers(Arrays.asList("device-token-123"))
            .title("标题")
            .content(null)
            .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
    }

    @Test
    void testSendResponseFields() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .bizId("push-006")
            .channel(NotificationChannel.PUSH)
            .receivers(Arrays.asList("device-token-123"))
            .title("测试")
            .content("测试内容")
            .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then - 验证所有必要字段
        assertThat(response.getSuccess()).isNotNull();
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getBizId()).isNotNull();
        assertThat(response.getProvider()).isNotNull();
        assertThat(response.getMessageId()).isNotNull();
        assertThat(response.getSendTime()).isNotNull();
    }

    @Test
    void testSendWithTemplateParams() {
        // Given
        HashMap<String, Object> params = new HashMap<>();
        params.put("userName", "张三");
        params.put("message", "您有一条新消息");

        NotificationRequest request = NotificationRequest.builder()
            .bizId("push-007")
            .channel(NotificationChannel.PUSH)
            .receivers(Arrays.asList("device-token-123"))
            .title("消息通知")
            .content("${userName}，${message}")
            .templateParams(params)
            .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
    }

    @Test
    void testSendWithPriority() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .bizId("push-008")
            .channel(NotificationChannel.PUSH)
            .receivers(Arrays.asList("device-token-123"))
            .title("紧急通知")
            .content("这是一条紧急通知")
            .priority(10)
            .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getProvider()).isEqualTo("fcm");
    }

    @Test
    void testSendWithLongContent() {
        // Given
        String longContent = "这是一条很长的内容".repeat(50);
        NotificationRequest request = NotificationRequest.builder()
            .bizId("push-009")
            .channel(NotificationChannel.PUSH)
            .receivers(Arrays.asList("device-token-123"))
            .title("长内容测试")
            .content(longContent)
            .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
    }
}

