/*-
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

import io.github.loadup.components.resilience4j.Resilience4jProperties;
import io.github.loadup.components.resilience4j.ResilienceRegistries;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedTimeLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.spring6.bulkhead.configure.BulkheadConfiguration;
import io.github.resilience4j.spring6.circuitbreaker.configure.CircuitBreakerConfiguration;
import io.github.resilience4j.spring6.ratelimiter.configure.RateLimiterConfiguration;
import io.github.resilience4j.spring6.retry.configure.RetryConfiguration;
import io.github.resilience4j.spring6.timelimiter.configure.TimeLimiterConfiguration;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Core binder auto-configuration for Resilience4j.
 *
 * <p>Assembles the five standard registries (circuit breaker / retry / rate limiter / bulkhead /
 * time limiter), their Spring AOP aspects and Micrometer metrics from the standard
 * {@code resilience4j.*} properties. Unlike the upstream {@code spring-boot3} starter, the
 * assembly lives in LoadUp so it stays compatible with Spring Boot 4.
 *
 * <p>Business code uses the standard Resilience4j annotations
 * ({@code @CircuitBreaker}, {@code @Retry}, {@code @RateLimiter}, {@code @Bulkhead},
 * {@code @TimeLimiter}) or injects the registries through {@link ResilienceRegistries}.
 */
@AutoConfiguration
@EnableConfigurationProperties({
    Resilience4jProperties.class,
    CircuitBreakerProperties.class,
    RetryProperties.class,
    RateLimiterProperties.class,
    BulkheadProperties.class,
    ThreadPoolBulkheadProperties.class,
    TimeLimiterProperties.class
})
@Import({
    CircuitBreakerConfiguration.class,
    RetryConfiguration.class,
    RateLimiterConfiguration.class,
    BulkheadConfiguration.class,
    TimeLimiterConfiguration.class
})
@ConditionalOnClass(CircuitBreakerRegistry.class)
@ConditionalOnProperty(prefix = "loadup.resilience4j", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Resilience4jCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ResilienceRegistries resilienceRegistries(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            RateLimiterRegistry rateLimiterRegistry,
            BulkheadRegistry bulkheadRegistry,
            TimeLimiterRegistry timeLimiterRegistry) {
        return new ResilienceRegistries(
                circuitBreakerRegistry, retryRegistry, rateLimiterRegistry, bulkheadRegistry, timeLimiterRegistry);
    }

    @Configuration
    @ConditionalOnClass(MeterRegistry.class)
    static class Resilience4jMetricsConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "circuitBreakerMetrics")
        public MeterBinder circuitBreakerMetrics(CircuitBreakerRegistry registry) {
            return TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry);
        }

        @Bean
        @ConditionalOnMissingBean(name = "retryMetrics")
        public MeterBinder retryMetrics(RetryRegistry registry) {
            return TaggedRetryMetrics.ofRetryRegistry(registry);
        }

        @Bean
        @ConditionalOnMissingBean(name = "rateLimiterMetrics")
        public MeterBinder rateLimiterMetrics(RateLimiterRegistry registry) {
            return TaggedRateLimiterMetrics.ofRateLimiterRegistry(registry);
        }

        @Bean
        @ConditionalOnMissingBean(name = "bulkheadMetrics")
        public MeterBinder bulkheadMetrics(BulkheadRegistry registry) {
            return TaggedBulkheadMetrics.ofBulkheadRegistry(registry);
        }

        @Bean
        @ConditionalOnMissingBean(name = "timeLimiterMetrics")
        public MeterBinder timeLimiterMetrics(TimeLimiterRegistry registry) {
            return TaggedTimeLimiterMetrics.ofTimeLimiterRegistry(registry);
        }
    }
}
