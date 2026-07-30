package io.github.loadup.gateway.core.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CircuitBreakerFilter.CircuitBreaker")
class CircuitBreakerFilterTest {

    CircuitBreakerFilter.CBConfig config(int failThreshold, int openTimeout, int halfOpenMax, int successThreshold) {
        CircuitBreakerFilter.CBConfig c = new CircuitBreakerFilter.CBConfig();
        c.enabled = true;
        c.failureThreshold = failThreshold;
        c.openTimeoutSeconds = openTimeout;
        c.halfOpenMax = halfOpenMax;
        c.successThreshold = successThreshold;
        return c;
    }

    @Nested
    @DisplayName("CLOSED to OPEN")
    class ClosedToOpen {

        @Test
        @DisplayName("opens after threshold failures")
        void opensAfterThreshold() {
            CircuitBreakerFilter.CBConfig c = config(3, 30, 3, 2);
            CircuitBreakerFilter.CircuitBreaker cb = new CircuitBreakerFilter.CircuitBreaker(c, Clock.systemUTC());

            assertThat(cb.allowRequest()).isTrue();
            cb.recordFailure();
            assertThat(cb.allowRequest()).isTrue();
            cb.recordFailure();
            assertThat(cb.allowRequest()).isTrue();
            cb.recordFailure();

            assertThat(cb.allowRequest()).isFalse();
        }

        @Test
        @DisplayName("success resets failure count")
        void successResetsCount() {
            CircuitBreakerFilter.CBConfig c = config(5, 30, 3, 2);
            CircuitBreakerFilter.CircuitBreaker cb = new CircuitBreakerFilter.CircuitBreaker(c, Clock.systemUTC());

            for (int i = 0; i < 3; i++) {
                cb.allowRequest();
                cb.recordFailure();
            }
            cb.allowRequest();
            cb.recordSuccess();
            for (int i = 0; i < 4; i++) {
                cb.allowRequest();
                cb.recordFailure();
            }
            assertThat(cb.allowRequest()).isTrue();
        }
    }

    @Nested
    @DisplayName("lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("OPEN to HALF_OPEN after timeout then CLOSED after successes")
        void fullLifecycle() {
            Instant now = Instant.parse("2026-01-01T00:00:00Z");
            CircuitBreakerFilter.CBConfig c = config(2, 10, 3, 2);
            CircuitBreakerFilter.CircuitBreaker cb =
                    new CircuitBreakerFilter.CircuitBreaker(c, Clock.fixed(now, ZoneId.of("UTC")));

            cb.allowRequest();
            cb.recordFailure();
            cb.allowRequest();
            cb.recordFailure();
            assertThat(cb.allowRequest()).isFalse();

            CircuitBreakerFilter.CircuitBreaker cb2 =
                    new CircuitBreakerFilter.CircuitBreaker(c, Clock.fixed(now.plusSeconds(15), ZoneId.of("UTC")));

            assertThat(cb2.allowRequest()).isTrue();
            cb2.recordSuccess();
            assertThat(cb2.allowRequest()).isTrue();
            cb2.recordSuccess();
            assertThat(cb2.allowRequest()).isTrue();
        }
    }
}
