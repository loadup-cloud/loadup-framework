package io.github.loadup.components.gotone.binder;

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
import static org.mockito.Mockito.when;

import io.github.loadup.components.gotone.api.INotificationProvider;
import io.github.loadup.components.gotone.binder.email.SmtpEmailProvider;
import io.github.loadup.components.gotone.binder.push.FcmPushProvider;
import io.github.loadup.components.gotone.binder.sms.AliyunSmsProvider;
import io.github.loadup.components.gotone.binder.sms.HuaweiSmsProvider;
import io.github.loadup.components.gotone.binder.sms.TencentSmsProvider;
import io.github.loadup.components.gotone.binder.sms.YunpianSmsProvider;
import io.github.loadup.components.gotone.enums.NotificationChannel;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

/** 所有 Provider 的集成测试 测试所有类型 Provider 的通用行为 */
class AllProvidersIntegrationTest {

    static Stream<Arguments> provideAllProviders() {
        // SMS Providers
        AliyunSmsProvider aliyun = new AliyunSmsProvider();
        ReflectionTestUtils.setField(aliyun, "accessKeyId", "test-key");
        ReflectionTestUtils.setField(aliyun, "accessKeySecret", "test-secret");

        TencentSmsProvider tencent = new TencentSmsProvider();
        ReflectionTestUtils.setField(tencent, "secretId", "test-id");
        ReflectionTestUtils.setField(tencent, "secretKey", "test-key");

        HuaweiSmsProvider huawei = new HuaweiSmsProvider();
        ReflectionTestUtils.setField(huawei, "appKey", "test-key");
        ReflectionTestUtils.setField(huawei, "appSecret", "test-secret");

        YunpianSmsProvider yunpian = new YunpianSmsProvider();
        ReflectionTestUtils.setField(yunpian, "apiKey", "test-key");

        // Email Provider
        SmtpEmailProvider smtp = new SmtpEmailProvider();
        JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);

        // 创建 MimeMessage Mock
        Session session = Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        ReflectionTestUtils.setField(smtp, "mailSender", mailSender);
        ReflectionTestUtils.setField(smtp, "fromEmail", "test@example.com");

        // Push Provider
        FcmPushProvider fcm = new FcmPushProvider();
        ReflectionTestUtils.setField(fcm, "serverKey", "test-server-key");

        return Stream.of(
                Arguments.of(aliyun, "aliyun", "SMS"),
                Arguments.of(tencent, "tencent", "SMS"),
                Arguments.of(huawei, "huawei", "SMS"),
                Arguments.of(yunpian, "yunpian", "SMS"),
                Arguments.of(smtp, "smtp", "EMAIL"),
                Arguments.of(fcm, "fcm", "PUSH"));
    }

    @ParameterizedTest
    @MethodSource("provideAllProviders")
    void testAllProvidersCanSendSuccessfully(INotificationProvider provider, String providerName, String channel) {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .bizId("integration-test-001")
                .channel(NotificationChannel.valueOf(channel))
                .addressList(Arrays.asList("test-receiver"))
                .title("集成测试")
                .content("集成测试内容")
                .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getProvider()).isEqualTo(providerName);
    }

    @ParameterizedTest
    @MethodSource("provideAllProviders")
    void testAllProvidersReturnCorrectProviderName(
            INotificationProvider provider, String expectedName, String channel) {
        // When & Then
        assertThat(provider.getProviderName()).isEqualTo(expectedName);
    }

    @ParameterizedTest
    @MethodSource("provideAllProviders")
    void testAllProvidersAreAvailableWithValidConfig(
            INotificationProvider provider, String providerName, String channel) {
        // When & Then
        assertThat(provider.isAvailable())
                .as("Provider %s should be available with valid config", providerName)
                .isTrue();
    }

    @Test
    void testAllProvidersImplementInterface() {
        // 测试所有 Provider 都实现了 INotificationProvider 接口
        INotificationProvider[] providers = {
            new AliyunSmsProvider(),
            new TencentSmsProvider(),
            new HuaweiSmsProvider(),
            new YunpianSmsProvider(),
            new SmtpEmailProvider(),
            new FcmPushProvider()
        };

        for (INotificationProvider provider : providers) {
            assertThat(provider).isInstanceOf(INotificationProvider.class);
            assertThat(provider.getProviderName()).isNotNull();
        }
    }

    @Test
    void testAllProvidersHaveUniqueNames() {
        // Given
        String[] providerNames = {
            new AliyunSmsProvider().getProviderName(),
            new TencentSmsProvider().getProviderName(),
            new HuaweiSmsProvider().getProviderName(),
            new YunpianSmsProvider().getProviderName(),
            new SmtpEmailProvider().getProviderName(),
            new FcmPushProvider().getProviderName()
        };

        // When & Then
        assertThat(providerNames)
                .hasSize(6)
                .doesNotHaveDuplicates()
                .containsExactlyInAnyOrder("aliyun", "tencent", "huawei", "yunpian", "smtp", "fcm");
    }

    @Test
    void testProvidersByChannel() {
        // 按渠道分类测试
        INotificationProvider[] smsProviders = {
            new AliyunSmsProvider(), new TencentSmsProvider(), new HuaweiSmsProvider(), new YunpianSmsProvider()
        };

        INotificationProvider[] emailProviders = {new SmtpEmailProvider()};

        INotificationProvider[] pushProviders = {new FcmPushProvider()};

        assertThat(smsProviders).hasSize(4);
        assertThat(emailProviders).hasSize(1);
        assertThat(pushProviders).hasSize(1);
    }

    @Test
    void testAllProvidersReturnValidResponse() {
        // 测试所有 Provider 返回的响应都包含必要字段
        AliyunSmsProvider sms = new AliyunSmsProvider();
        ReflectionTestUtils.setField(sms, "accessKeyId", "test");
        ReflectionTestUtils.setField(sms, "accessKeySecret", "test");

        SmtpEmailProvider email = new SmtpEmailProvider();
        JavaMailSender mockMailSender = Mockito.mock(JavaMailSender.class);
        Session session = Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        when(mockMailSender.createMimeMessage()).thenReturn(mimeMessage);
        ReflectionTestUtils.setField(email, "mailSender", mockMailSender);
        ReflectionTestUtils.setField(email, "fromEmail", "test@example.com");

        FcmPushProvider push = new FcmPushProvider();
        ReflectionTestUtils.setField(push, "serverKey", "test");

        INotificationProvider[] providers = {sms, email, push};

        for (INotificationProvider provider : providers) {
            NotificationRequest request = NotificationRequest.builder()
                    .bizId("test")
                    .channel(NotificationChannel.SMS)
                    .addressList(Arrays.asList("test"))
                    .content("test")
                    .build();

            NotificationResponse response = provider.send(request);

            assertThat(response).isNotNull();
            assertThat(response.getProvider()).isNotNull();
            assertThat(response.getStatus()).isNotNull();
        }
    }
}
