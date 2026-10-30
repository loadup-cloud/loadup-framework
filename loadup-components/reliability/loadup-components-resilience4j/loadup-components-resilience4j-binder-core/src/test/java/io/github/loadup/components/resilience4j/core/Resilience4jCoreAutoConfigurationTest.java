/*
 * #%L
 * Loadup Resilience4j Binder Core
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
package io.github.loadup.components.resilience4j.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.resilience4j.Resilience4jProperties;
import io.github.loadup.components.resilience4j.ResilienceRegistries;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class Resilience4jCoreAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(Resilience4jCoreAutoConfiguration.class));

    @Test
    void assemblesRegistriesByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ResilienceRegistries.class);
            assertThat(context).hasSingleBean(CircuitBreakerRegistry.class);
            assertThat(context).hasSingleBean(RetryRegistry.class);
            assertThat(context.getBean(Resilience4jProperties.class).isEnabled())
                    .isTrue();
            assertThat(context.getBean(Resilience4jProperties.class).getBinderType())
                    .isEqualTo("core");
            assertThat(context.getBean(ResilienceRegistries.class).circuitBreakerRegistry())
                    .isSameAs(context.getBean(CircuitBreakerRegistry.class));
        });
    }

    @Test
    void disabledViaProperty() {
        contextRunner.withPropertyValues("loadup.resilience4j.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(ResilienceRegistries.class);
        });
    }

    @Test
    void consumerCanBindDisabledSettingsWhileRegistryAssemblyIsSkipped() {
        contextRunner
                .withUserConfiguration(ConsumerSettings.class)
                .withPropertyValues("loadup.resilience4j.enabled=false", "loadup.resilience4j.binder-type=core")
                .run(context -> {
                    assertThat(context).hasNotFailed().doesNotHaveBean(ResilienceRegistries.class);
                    assertThat(context.getBean(Resilience4jProperties.class).isEnabled())
                            .isFalse();
                    assertThat(context.getBean(Resilience4jProperties.class).getBinderType())
                            .isEqualTo("core");
                });
    }

    @Test
    void createsConfiguredCircuitBreakerInstance() {
        contextRunner
                .withPropertyValues(
                        "resilience4j.circuitbreaker.instances.demo.failure-rate-threshold=25",
                        "resilience4j.circuitbreaker.instances.demo.sliding-window-size=4")
                .run(context -> {
                    CircuitBreakerRegistry registry = context.getBean(CircuitBreakerRegistry.class);
                    assertThat(registry.getAllCircuitBreakers())
                            .anyMatch(circuitBreaker -> circuitBreaker.getName().equals("demo"));
                });
    }

    @Test
    void standaloneDefaultsProvideIndependentUsablePolicies() throws Exception {
        var policies = ResilienceRegistries.ofDefaults();
        var other = ResilienceRegistries.ofDefaults();
        var breaker = policies.circuitBreakerRegistry().circuitBreaker("payments");
        assertThat(breaker.executeSupplier(() -> "accepted")).isEqualTo("accepted");
        breaker.transitionToOpenState();
        assertThat(other.circuitBreakerRegistry().circuitBreaker("payments").getState())
                .isEqualTo(io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED);

        var attempts = new java.util.concurrent.atomic.AtomicInteger();
        assertThat(policies.retryRegistry().retry("notifications").executeSupplier(() -> {
                    if (attempts.incrementAndGet() == 1) {
                        throw new IllegalStateException("synthetic transient failure");
                    }
                    return "delivered";
                }))
                .isEqualTo("delivered");
        assertThat(attempts.get()).isEqualTo(2);
        var bulkhead = policies.bulkheadRegistry().bulkhead("exports");
        int permits = bulkhead.getMetrics().getAvailableConcurrentCalls();
        assertThat(bulkhead.executeSupplier(() -> bulkhead.getMetrics().getAvailableConcurrentCalls()))
                .isEqualTo(permits - 1);
        assertThat(bulkhead.getMetrics().getAvailableConcurrentCalls()).isEqualTo(permits);
        var limiter = policies.rateLimiterRegistry().rateLimiter("callbacks");
        assertThat(limiter.acquirePermission()).isTrue();
        assertThat(policies.timeLimiterRegistry()
                        .timeLimiter("channel")
                        .executeFutureSupplier(
                                () -> java.util.concurrent.CompletableFuture.completedFuture("completed")))
                .isEqualTo("completed");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(Resilience4jProperties.class)
    static class ConsumerSettings {}
}
