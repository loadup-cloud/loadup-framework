package io.github.loadup.components.gotone.binder.sms;

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
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** AliyunSmsProvider 测试类 */
class AliyunSmsProviderTest {

    private AliyunSmsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new AliyunSmsProvider();
        ReflectionTestUtils.setField(provider, "accessKeyId", "test-key-id");
        ReflectionTestUtils.setField(provider, "accessKeySecret", "test-key-secret");
        ReflectionTestUtils.setField(provider, "signName", "测试签名");
    }

    @Test
    void testSendSuccess() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("test-001")
                .channel(NotificationChannel.SMS)
                .addressList(Arrays.asList("13800138000", "13900139000"))
                .content("您的验证码是123456")
                .templateParams(new HashMap())
                .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(response.getBizId()).isEqualTo("test-001");
        assertThat(response.getProvider()).isEqualTo("aliyun");
        assertThat(response.getMessageId()).isNotNull();
        assertThat(response.getSendTime()).isNotNull();
        assertThat(response.getErrorMessage()).isNull();
    }

    @Test
    void testSendWithNullReceivers() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("test-002")
                .channel(NotificationChannel.SMS)
                .addressList(null)
                .content("测试内容")
                .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then - 应该正常处理，不抛异常
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
    }

    @Test
    void testSendWithEmptyContent() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("test-003")
                .channel(NotificationChannel.SMS)
                .addressList(Arrays.asList("13800138000"))
                .content("")
                .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
    }

    @Test
    void testGetProviderName() {
        // When & Then
        assertThat(provider.getProviderName()).isEqualTo("aliyun");
    }

    @Test
    void testIsAvailableWithValidConfig() {
        // When & Then
        assertThat(provider.isAvailable()).isTrue();
    }

    @Test
    void testIsAvailableWithNullAccessKeyId() {
        // Given
        ReflectionTestUtils.setField(provider, "accessKeyId", null);

        // When & Then
        assertThat(provider.isAvailable()).isFalse();
    }

    @Test
    void testIsAvailableWithEmptyAccessKeyId() {
        // Given
        ReflectionTestUtils.setField(provider, "accessKeyId", "");

        // When & Then
        assertThat(provider.isAvailable()).isFalse();
    }

    @Test
    void testSendWithMultipleReceivers() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("test-004")
                .channel(NotificationChannel.SMS)
                .addressList(Arrays.asList("13800138000", "13900139000", "13700137000"))
                .content("批量发送测试")
                .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getProvider()).isEqualTo("aliyun");
    }

    @Test
    void testSendWithTemplateParams() {
        // Given
        HashMap<String, Object> params = new HashMap<>();
        params.put("code", "123456");
        params.put("minutes", "5");

        NotificationRequest request = NotificationRequest.builder()
                .bizId("test-005")
                .channel(NotificationChannel.SMS)
                .addressList(Arrays.asList("13800138000"))
                .content("您的验证码是${code}，${minutes}分钟内有效")
                .templateParams(params)
                .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
    }

    @Test
    void testSendResponseFields() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("test-006")
                .channel(NotificationChannel.SMS)
                .addressList(Arrays.asList("13800138000"))
                .content("测试响应字段")
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
}
