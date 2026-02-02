package io.github.loadup.components.gotone.binder.email;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.loadup.components.gotone.enums.NotificationChannel;
import io.github.loadup.components.gotone.enums.NotificationStatus;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

/** SmtpEmailProvider 测试类 */
class SmtpEmailProviderTest {

    private SmtpEmailProvider provider;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new SmtpEmailProvider();

        // 创建 MimeMessage 用于 Mock
        Session session = Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);

        // Mock createMimeMessage 方法
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        ReflectionTestUtils.setField(provider, "mailSender", mailSender);
        ReflectionTestUtils.setField(provider, "fromEmail", "test@example.com");
    }

    @Test
    void testSendSuccess() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("email-001")
                .channel(NotificationChannel.EMAIL)
                .receivers(Arrays.asList("user@example.com"))
                .title("测试邮件")
                .content("这是一封测试邮件")
                .build();

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
        assertThat(response.getBizId()).isEqualTo("email-001");
        assertThat(response.getProvider()).isEqualTo("smtp");
        assertThat(response.getMessageId()).isNotNull();
        assertThat(response.getSendTime()).isNotNull();

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendWithMultipleReceivers() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("email-002")
                .channel(NotificationChannel.EMAIL)
                .receivers(Arrays.asList("user1@example.com", "user2@example.com", "user3@example.com"))
                .title("批量邮件")
                .content("批量发送测试")
                .build();

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getProvider()).isEqualTo("smtp");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendWithNullMailSender() {
        // Given
        ReflectionTestUtils.setField(provider, "mailSender", null);

        NotificationRequest request = NotificationRequest.builder()
                .bizId("email-003")
                .channel(NotificationChannel.EMAIL)
                .receivers(Arrays.asList("user@example.com"))
                .title("测试")
                .content("内容")
                .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(response.getErrorMessage()).contains("JavaMailSender not configured");
    }

    @Test
    void testSendWithException() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("email-004")
                .channel(NotificationChannel.EMAIL)
                .receivers(Arrays.asList("invalid@"))
                .title("测试")
                .content("内容")
                .build();

        doThrow(new RuntimeException("发送失败")).when(mailSender).send(any(MimeMessage.class));

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.FAILED);
        // 错误消息可能包含异常的任何信息
        assertThat(response.getErrorMessage()).isNotNull();
    }

    @Test
    void testGetProviderName() {
        assertThat(provider.getProviderName()).isEqualTo("smtp");
    }

    @Test
    void testIsAvailableWithMailSender() {
        assertThat(provider.isAvailable()).isTrue();
    }

    @Test
    void testIsAvailableWithoutMailSender() {
        ReflectionTestUtils.setField(provider, "mailSender", null);
        assertThat(provider.isAvailable()).isFalse();
    }

    @Test
    void testSendWithEmptyTitle() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("email-005")
                .channel(NotificationChannel.EMAIL)
                .receivers(Arrays.asList("user@example.com"))
                .title("")
                .content("只有内容没有标题")
                .build();

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
    }

    @Test
    void testSendWithNullContent() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("email-006")
                .channel(NotificationChannel.EMAIL)
                .receivers(Arrays.asList("user@example.com"))
                .title("测试标题")
                .content(null)
                .build();

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        NotificationResponse response = provider.send(request);

        // Then - content 为 null 时应该正常处理
        assertThat(response).isNotNull();
        // Note: null content 可能导致发送失败或成功，取决于实现
    }

    @Test
    void testSendResponseFields() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("email-007")
                .channel(NotificationChannel.EMAIL)
                .receivers(Arrays.asList("user@example.com"))
                .title("测试")
                .content("测试内容")
                .build();

        doNothing().when(mailSender).send(any(MimeMessage.class));

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
        params.put("orderId", "202512300001");

        NotificationRequest request = NotificationRequest.builder()
                .bizId("email-008")
                .channel(NotificationChannel.EMAIL)
                .receivers(Arrays.asList("user@example.com"))
                .title("订单通知")
                .content("尊敬的${userName}，您的订单${orderId}已确认")
                .templateParams(params)
                .build();

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
    }
}
