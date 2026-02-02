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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** TencentSmsProvider 测试类 */
class TencentSmsProviderTest {

  private TencentSmsProvider provider;

  @BeforeEach
  void setUp() {
    provider = new TencentSmsProvider();
    ReflectionTestUtils.setField(provider, "secretId", "test-secret-id");
    ReflectionTestUtils.setField(provider, "secretKey", "test-secret-key");
    ReflectionTestUtils.setField(provider, "sdkAppId", "1400000000");
    ReflectionTestUtils.setField(provider, "signName", "腾讯云");
  }

  @Test
  void testSendSuccess() {
    // Given
    NotificationRequest request =
        NotificationRequest.builder()
            .bizId("tencent-001")
            .channel(NotificationChannel.SMS)
            .receivers(Arrays.asList("13800138000"))
            .content("腾讯云短信测试")
            .build();

    // When
    NotificationResponse response = provider.send(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
    assertThat(response.getProvider()).isEqualTo("tencent");
  }

  @Test
  void testGetProviderName() {
    assertThat(provider.getProviderName()).isEqualTo("tencent");
  }

  @Test
  void testIsAvailableWithValidConfig() {
    assertThat(provider.isAvailable()).isTrue();
  }

  @Test
  void testIsAvailableWithNullSecretId() {
    ReflectionTestUtils.setField(provider, "secretId", null);
    assertThat(provider.isAvailable()).isFalse();
  }

  @Test
  void testIsAvailableWithEmptySecretKey() {
    ReflectionTestUtils.setField(provider, "secretKey", "");
    assertThat(provider.isAvailable()).isFalse();
  }

  @Test
  void testIsAvailableWithBothNull() {
    ReflectionTestUtils.setField(provider, "secretId", null);
    ReflectionTestUtils.setField(provider, "secretKey", null);
    assertThat(provider.isAvailable()).isFalse();
  }

  @Test
  void testSendWithMultipleReceivers() {
    // Given
    NotificationRequest request =
        NotificationRequest.builder()
            .bizId("tencent-002")
            .channel(NotificationChannel.SMS)
            .receivers(Arrays.asList("13800138000", "13900139000"))
            .content("批量短信")
            .build();

    // When
    NotificationResponse response = provider.send(request);

    // Then
    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getMessageId()).isNotNull();
  }
}
