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

import io.github.loadup.components.gotone.api.INotificationProvider;
import io.github.loadup.components.gotone.enums.NotificationChannel;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * SMS Provider 集成测试
 * 测试所有 SMS Provider 的通用行为
 */
class SmsProviderIntegrationTest {

    static Stream<Arguments> provideAllSmsProviders() {
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

        return Stream.of(
            Arguments.of(aliyun, "aliyun"),
            Arguments.of(tencent, "tencent"),
            Arguments.of(huawei, "huawei"),
            Arguments.of(yunpian, "yunpian")
        );
    }

    @ParameterizedTest
    @MethodSource("provideAllSmsProviders")
    void testAllProvidersCanSendSuccessfully(INotificationProvider provider, String providerName) {
        // Given
        NotificationRequest request = NotificationRequest.builder()
            .bizId("integration-test-001")
            .channel(NotificationChannel.SMS)
            .receivers(Arrays.asList("13800138000"))
            .content("集成测试")
            .build();

        // When
        NotificationResponse response = provider.send(request);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getProvider()).isEqualTo(providerName);
    }

    @ParameterizedTest
    @MethodSource("provideAllSmsProviders")
    void testAllProvidersReturnCorrectProviderName(INotificationProvider provider, String expectedName) {
        // When & Then
        assertThat(provider.getProviderName()).isEqualTo(expectedName);
    }

    @ParameterizedTest
    @MethodSource("provideAllSmsProviders")
    void testAllProvidersAreAvailableWithValidConfig(INotificationProvider provider, String providerName) {
        // When & Then
        assertThat(provider.isAvailable())
            .as("Provider %s should be available with valid config", providerName)
            .isTrue();
    }

    @Test
    void testProviderCompatibility() {
        // 测试所有 Provider 都实现了 INotificationProvider 接口
        INotificationProvider[] providers = {
            new AliyunSmsProvider(),
            new TencentSmsProvider(),
            new HuaweiSmsProvider(),
            new YunpianSmsProvider()
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
            new YunpianSmsProvider().getProviderName()
        };

        // When & Then
        assertThat(providerNames)
            .hasSize(4)
            .doesNotHaveDuplicates()
            .containsExactlyInAnyOrder("aliyun", "tencent", "huawei", "yunpian");
    }
}

