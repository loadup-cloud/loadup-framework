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

/** YunpianSmsProvider 测试类 */
class YunpianSmsProviderTest {

  private YunpianSmsProvider provider;

  @BeforeEach
  void setUp() {
    provider = new YunpianSmsProvider();
    ReflectionTestUtils.setField(provider, "apiKey", "test-api-key");
    ReflectionTestUtils.setField(
        provider, "apiUrl", "https://sms.yunpian.com/v2/sms/single_send.json");
  }

  @Test
  void testSendSuccess() {
    // Given
    NotificationRequest request =
        NotificationRequest.builder()
            .bizId("yunpian-001")
            .channel(NotificationChannel.SMS)
            .receivers(Arrays.asList("13800138000"))
            .content("云片短信测试")
            .build();

    // When
    NotificationResponse response = provider.send(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
    assertThat(response.getProvider()).isEqualTo("yunpian");
  }

  @Test
  void testGetProviderName() {
    assertThat(provider.getProviderName()).isEqualTo("yunpian");
  }

  @Test
  void testIsAvailableWithValidConfig() {
    assertThat(provider.isAvailable()).isTrue();
  }

  @Test
  void testIsAvailableWithNullApiKey() {
    ReflectionTestUtils.setField(provider, "apiKey", null);
    assertThat(provider.isAvailable()).isFalse();
  }

  @Test
  void testIsAvailableWithEmptyApiKey() {
    ReflectionTestUtils.setField(provider, "apiKey", "");
    assertThat(provider.isAvailable()).isFalse();
  }

  @Test
  void testSendWithCustomApiUrl() {
    // Given
    String customUrl = "https://custom.yunpian.com/api";
    ReflectionTestUtils.setField(provider, "apiUrl", customUrl);

    NotificationRequest request =
        NotificationRequest.builder()
            .bizId("yunpian-002")
            .channel(NotificationChannel.SMS)
            .receivers(Arrays.asList("13800138000"))
            .content("测试")
            .build();

    // When
    NotificationResponse response = provider.send(request);

    // Then
    assertThat(response.getSuccess()).isTrue();
  }

  @Test
  void testSendWithMultipleReceivers() {
    // Given
    NotificationRequest request =
        NotificationRequest.builder()
            .bizId("yunpian-003")
            .channel(NotificationChannel.SMS)
            .receivers(Arrays.asList("13800138000", "13900139000", "13700137000"))
            .content("批量发送")
            .build();

    // When
    NotificationResponse response = provider.send(request);

    // Then
    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getMessageId()).isNotNull();
    assertThat(response.getSendTime()).isNotNull();
  }
}
