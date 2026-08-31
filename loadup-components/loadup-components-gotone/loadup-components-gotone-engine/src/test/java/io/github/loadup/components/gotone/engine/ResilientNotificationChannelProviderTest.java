/*-
 * #%L
 * Loadup Gotone Engine
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
package io.github.loadup.components.gotone.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import io.github.loadup.components.resilience4j.ResilienceRegistries;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ResilientNotificationChannelProviderTest {

    private static final ChannelSendRequest REQUEST =
            new ChannelSendRequest(List.of("receiver-1"), "content", Map.of(), Map.of());

    @Test
    void retriesTheSameProviderBeforeFallingThrough() {
        FlakyProvider delegate = new FlakyProvider(2);
        ResilientNotificationChannelProvider provider =
                ResilientNotificationChannelProvider.wrap(delegate, registries());

        ChannelSendResponse response = provider.send(REQUEST);

        assertThat(response.successCount()).isEqualTo(1);
        assertThat(delegate.calls()).isEqualTo(3);
    }

    @Test
    void opensCircuitBreakerAndSkipsTheProvider() {
        FlakyProvider delegate = new FlakyProvider(Integer.MAX_VALUE);
        ResilientNotificationChannelProvider provider =
                ResilientNotificationChannelProvider.wrap(delegate, registries());

        assertThatThrownBy(() -> provider.send(REQUEST)).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> provider.send(REQUEST)).isInstanceOf(RuntimeException.class);

        assertThatThrownBy(() -> provider.send(REQUEST)).isInstanceOf(CallNotPermittedException.class);
        assertThat(delegate.calls()).isEqualTo(6);
    }

    @Test
    void reportsUnavailableWhileCircuitIsOpen() {
        FlakyProvider delegate = new FlakyProvider(Integer.MAX_VALUE);
        ResilientNotificationChannelProvider provider =
                ResilientNotificationChannelProvider.wrap(delegate, registries());

        try {
            provider.send(REQUEST);
        } catch (RuntimeException expected) {
            // expected
        }
        try {
            provider.send(REQUEST);
        } catch (RuntimeException expected) {
            // expected
        }

        assertThat(provider.isAvailable()).isFalse();
    }

    @Test
    void instanceNameFollowsTheProviderConvention() {
        FlakyProvider delegate = new FlakyProvider(0);
        assertThat(ResilientNotificationChannelProvider.instanceName(delegate)).isEqualTo("gotone-sms-stub");
    }

    private static ResilienceRegistries registries() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .waitDurationInOpenState(Duration.ofSeconds(1))
                .permittedNumberOfCallsInHalfOpenState(1)
                .build());
        RetryRegistry retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(10))
                .build());
        return new ResilienceRegistries(
                circuitBreakerRegistry,
                retryRegistry,
                RateLimiterRegistry.ofDefaults(),
                BulkheadRegistry.ofDefaults(),
                TimeLimiterRegistry.ofDefaults());
    }

    private static final class FlakyProvider implements NotificationChannelProvider {
        private final int failuresBeforeSuccess;
        private final AtomicInteger calls = new AtomicInteger();

        private FlakyProvider(int failuresBeforeSuccess) {
            this.failuresBeforeSuccess = failuresBeforeSuccess;
        }

        @Override
        public String getChannelType() {
            return "sms";
        }

        @Override
        public String getProviderName() {
            return "stub";
        }

        @Override
        public ChannelSendResponse send(ChannelSendRequest request) {
            int current = calls.incrementAndGet();
            if (current <= failuresBeforeSuccess) {
                throw new IllegalStateException("provider down");
            }
            return new ChannelSendResponse("content", 1, 0, Map.of("receiver-1", true), Map.of());
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        int calls() {
            return calls.get();
        }
    }
}
