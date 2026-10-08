/*
 * #%L
 * LoadUp Components Signature
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.signature.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisRequestNonceStoreTest {
    @Test
    @SuppressWarnings("unchecked")
    void roundsExpiryUpAndTreatsNullAsFailure() {
        var redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        String key = "a".repeat(64);
        String storageKey = "loadup:signature:nonce:" + key;
        when(values.setIfAbsent(storageKey, "1", Duration.ofMillis(2))).thenReturn(true, false, null);
        var store = new RedisRequestNonceStore(redis);
        assertThat(store.claim(key, Duration.ofNanos(1000001))).isTrue();
        assertThat(store.claim(key, Duration.ofNanos(1000001))).isFalse();
        assertThatThrownBy(() -> store.claim(key, Duration.ofNanos(1000001))).isInstanceOf(IllegalStateException.class);
        verify(values, org.mockito.Mockito.times(3)).setIfAbsent(storageKey, "1", Duration.ofMillis(2));
    }
}
