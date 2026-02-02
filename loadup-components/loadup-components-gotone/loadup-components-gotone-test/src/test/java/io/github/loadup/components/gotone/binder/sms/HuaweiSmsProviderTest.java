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

/** HuaweiSmsProvider 测试类 */
class HuaweiSmsProviderTest {

  private HuaweiSmsProvider provider;

  @BeforeEach
  void setUp() {
    provider = new HuaweiSmsProvider();
    ReflectionTestUtils.setField(provider, "appKey", "test-app-key");
    ReflectionTestUtils.setField(provider, "appSecret", "test-app-secret");
    ReflectionTestUtils.setField(provider, "sender", "test-sender");
    ReflectionTestUtils.setField(provider, "signature", "华为云");
  }

  @Test
  void testSendSuccess() {
    // Given
    NotificationRequest request =
        NotificationRequest.builder()
            .bizId("huawei-001")
            .channel(NotificationChannel.SMS)
            .receivers(Arrays.asList("13800138000"))
            .content("华为云短信测试")
            .build();

    // When
    NotificationResponse response = provider.send(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
    assertThat(response.getProvider()).isEqualTo("huawei");
  }

  @Test
  void testGetProviderName() {
    assertThat(provider.getProviderName()).isEqualTo("huawei");
  }

  @Test
  void testIsAvailableWithValidConfig() {
    assertThat(provider.isAvailable()).isTrue();
  }

  @Test
  void testIsAvailableWithNullAppKey() {
    ReflectionTestUtils.setField(provider, "appKey", null);
    assertThat(provider.isAvailable()).isFalse();
  }

  @Test
  void testIsAvailableWithEmptyAppSecret() {
    ReflectionTestUtils.setField(provider, "appSecret", "");
    assertThat(provider.isAvailable()).isFalse();
  }

  @Test
  void testSendResponseContainsAllFields() {
    // Given
    NotificationRequest request =
        NotificationRequest.builder()
            .bizId("huawei-002")
            .channel(NotificationChannel.SMS)
            .receivers(Arrays.asList("13800138000"))
            .content("测试")
            .build();

    // When
    NotificationResponse response = provider.send(request);

    // Then
    assertThat(response.getBizId()).isEqualTo("huawei-002");
    assertThat(response.getMessageId()).isNotNull();
    assertThat(response.getSendTime()).isNotNull();
  }
}
