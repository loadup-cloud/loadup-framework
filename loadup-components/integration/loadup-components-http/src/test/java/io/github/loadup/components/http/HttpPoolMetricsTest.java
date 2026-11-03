/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class HttpPoolMetricsTest {
    private SimpleMeterRegistry registry;

    @AfterEach
    void closeRegistry() {
        if (registry != null) {
            registry.close();
        }
    }

    @Test
    void gaugesFollowReplacementWhileTimersSurviveAndRemovedClientsReleaseMeters() {
        var clock = new MockClock();
        registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, clock);
        try (var metrics = new HttpPoolMetrics(registry, "application");
                var first = PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(2)
                        .build();
                var replacement = PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(7)
                        .build()) {
            var old = metrics.candidate("orders", first);
            metrics.activate(Map.of("orders", old));
            var max = registry.get("loadup.http.pool.max").gauge();
            var timer = registry.get("loadup.http.pool.acquire")
                    .tag("outcome", "success")
                    .timer();
            long start = old.start();
            clock.add(Duration.ofMillis(25));
            old.record(start, "success");

            var next = metrics.candidate("orders", replacement);
            metrics.activate(Map.of("orders", next));
            first.close();
            assertThat(max.value()).isEqualTo(7);
            start = next.start();
            clock.add(Duration.ofMillis(35));
            next.record(start, "success");
            assertThat(registry.get("loadup.http.pool.acquire")
                            .tag("outcome", "success")
                            .timer())
                    .isSameAs(timer);
            assertThat(timer.count()).isEqualTo(2);
            assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(60);
            assertThat(registry.getMeters()).hasSize(9);

            metrics.activate(Map.of());
            assertThat(registry.getMeters()).isEmpty();
            assertThat(max.value()).isZero();
        }
    }

    @Test
    void registrationFailureRollsBackCandidateMetersAndKeepsActivePool() {
        registry = new SimpleMeterRegistry();
        try (var metrics = new HttpPoolMetrics(registry, "application");
                var first = PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(2)
                        .build();
                var replacement = PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(7)
                        .build()) {
            metrics.activate(Map.of("orders", metrics.candidate("orders", first)));
            registry.config().meterFilter(new MeterFilter() {
                @Override
                public Meter.Id map(Meter.Id id) {
                    if ("broken".equals(id.getTag("client")) && id.getName().endsWith(".pending")) {
                        throw new IllegalStateException("synthetic registration failure");
                    }
                    return id;
                }
            });
            assertThatThrownBy(() -> metrics.activate(Map.of(
                            "orders", metrics.candidate("orders", replacement),
                            "broken", metrics.candidate("broken", replacement))))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(registry.get("loadup.http.pool.max")
                            .tag("client", "orders")
                            .gauge()
                            .value())
                    .isEqualTo(2);
            assertThat(registry.getMeters()).hasSize(9);
            assertThat(registry.getMeters())
                    .allSatisfy(
                            meter -> assertThat(meter.getId().getTag("client")).isEqualTo("orders"));
        }
    }

    @Test
    void conflictingTemplateCannotRemoveAnotherOwnersMetersAndScopesCanCoexist() {
        registry = new SimpleMeterRegistry();
        try (var owner = new HttpPoolMetrics(registry, "application");
                var conflicting = new HttpPoolMetrics(registry, "application");
                var isolated = new HttpPoolMetrics(registry, "openbao-transit");
                var first = PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(2)
                        .build();
                var second = PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(7)
                        .build()) {
            owner.activate(Map.of("kms", owner.candidate("kms", first)));
            assertThatThrownBy(() -> conflicting.activate(Map.of("kms", conflicting.candidate("kms", second))))
                    .isInstanceOf(IllegalArgumentException.class);
            conflicting.close();
            assertThat(registry.get("loadup.http.pool.max")
                            .tag("scope", "application")
                            .gauge()
                            .value())
                    .isEqualTo(2);
            isolated.activate(Map.of("kms", isolated.candidate("kms", second)));
            owner.close();
            assertThat(registry.getMeters()).hasSize(9);
            assertThat(registry.get("loadup.http.pool.max")
                            .tag("scope", "openbao-transit")
                            .gauge()
                            .value())
                    .isEqualTo(7);
        }
    }

    @Test
    void acquireTimerStopsBeforeConnectAndRepeatedGetDoesNotCountTwice() throws Exception {
        var clock = new MockClock();
        registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, clock);
        try (var metrics = new HttpPoolMetrics(registry, "application");
                var pool = PoolingHttpClientConnectionManagerBuilder.create().build()) {
            var state = metrics.candidate("orders", pool);
            metrics.activate(Map.of("orders", state));
            var delegate = mock(HttpClientConnectionManager.class);
            var lease = mock(LeaseRequest.class);
            var endpoint = mock(ConnectionEndpoint.class);
            when(delegate.lease(anyString(), any(), any(), any())).thenReturn(lease);
            when(lease.get(any())).thenAnswer(invocation -> {
                clock.add(Duration.ofMillis(40));
                return endpoint;
            });
            doAnswer(invocation -> {
                        clock.add(Duration.ofSeconds(10));
                        return null;
                    })
                    .when(delegate)
                    .connect(any(), any(), any());
            var manager = new MeteredConnectionManager(delegate, state);
            var acquired = manager.lease(
                    "lease", new HttpRoute(new HttpHost("http", "unused.invalid", 80)), Timeout.ofSeconds(1), null);
            assertThat(acquired.get(Timeout.ofSeconds(1))).isSameAs(endpoint);
            manager.connect(endpoint, Timeout.ofSeconds(1), null);
            acquired.get(Timeout.ofSeconds(1));
            var timer = registry.get("loadup.http.pool.acquire")
                    .tag("outcome", "success")
                    .timer();
            assertThat(timer.count()).isEqualTo(1);
            assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(40);
        }
    }

    @Test
    void nativeLeaseReportsPendingTimeoutAndReleaseWithoutOpeningAnySocket() throws Exception {
        registry = new SimpleMeterRegistry();
        try (var metrics = new HttpPoolMetrics(registry, "application");
                var pool = PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(1)
                        .setMaxConnPerRoute(1)
                        .build();
                var executor = Executors.newSingleThreadExecutor()) {
            var state = metrics.candidate("orders", pool);
            metrics.activate(Map.of("orders", state));
            var manager = new MeteredConnectionManager(pool, state);
            // A lease can be acquired disconnected: no DNS, TCP or HTTP request is made.
            var route = new HttpRoute(new HttpHost("http", "unused.invalid", 80));
            var held = manager.lease("first", route, Timeout.ofSeconds(2), null).get(Timeout.ofSeconds(2));
            try {
                assertThat(held.isConnected()).isFalse();
                var waiting = executor.submit(() -> {
                    var lease = manager.lease("waiting", route, Timeout.ofSeconds(2), null);
                    assertThatThrownBy(() -> lease.get(Timeout.ofSeconds(2))).isInstanceOf(TimeoutException.class);
                });
                await().atMost(Duration.ofSeconds(1))
                        .untilAsserted(() -> assertThat(registry.get("loadup.http.pool.pending")
                                        .gauge()
                                        .value())
                                .isEqualTo(1));
                assertThat(registry.get("loadup.http.pool.leased").gauge().value())
                        .isEqualTo(1);
                waiting.get(3, TimeUnit.SECONDS);
                assertThat(registry.get("loadup.http.pool.acquire")
                                .tag("outcome", "timeout")
                                .timer()
                                .count())
                        .isEqualTo(1);
                assertThat(registry.get("loadup.http.pool.acquire")
                                .tag("outcome", "success")
                                .timer()
                                .count())
                        .isEqualTo(1);
                assertThat(registry.get("loadup.http.pool.pending").gauge().value())
                        .isZero();
            } finally {
                manager.release(held, null, TimeValue.ZERO_MILLISECONDS);
            }
            assertThat(registry.get("loadup.http.pool.leased").gauge().value()).isZero();
        }
    }
}
