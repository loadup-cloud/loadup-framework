package io.github.loadup.components.resilience4j;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

/**
 * Assembly contract produced by a resilience4j binder.
 *
 * <p>Consumers depend on the API module and inject this carrier instead of five individual
 * registries. The active binder decides whether state is local (default core binder) or
 * distributed (future Redis binder).
 */
public record ResilienceRegistries(
        CircuitBreakerRegistry circuitBreakerRegistry,
        RetryRegistry retryRegistry,
        RateLimiterRegistry rateLimiterRegistry,
        BulkheadRegistry bulkheadRegistry,
        TimeLimiterRegistry timeLimiterRegistry) {

    public static ResilienceRegistries ofDefaults() {
        return new ResilienceRegistries(
                CircuitBreakerRegistry.ofDefaults(),
                RetryRegistry.ofDefaults(),
                RateLimiterRegistry.ofDefaults(),
                BulkheadRegistry.ofDefaults(),
                TimeLimiterRegistry.ofDefaults());
    }
}
