package io.github.loadup.gateway.core.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RateLimitFilter.TokenBucket")
class TokenBucketTest {

    @Nested
    @DisplayName("basic operations")
    class BasicOps {

        @Test
        @DisplayName("acquires tokens within capacity")
        void acquiresWithinCapacity() {
            RateLimitFilter.TokenBucket bucket = new RateLimitFilter.TokenBucket(5, 10.0);
            for (int i = 0; i < 5; i++) {
                assertThat(bucket.tryAcquire()).isTrue();
            }
        }

        @Test
        @DisplayName("rejects when empty")
        void rejectsWhenEmpty() {
            RateLimitFilter.TokenBucket bucket = new RateLimitFilter.TokenBucket(2, 0.0);
            assertThat(bucket.tryAcquire()).isTrue();
            assertThat(bucket.tryAcquire()).isTrue();
            assertThat(bucket.tryAcquire()).isFalse();
        }

        @Test
        @DisplayName("refills over time")
        void refillsOverTime() throws InterruptedException {
            RateLimitFilter.TokenBucket bucket = new RateLimitFilter.TokenBucket(3, 100.0);
            for (int i = 0; i < 3; i++) {
                assertThat(bucket.tryAcquire()).isTrue();
            }
            assertThat(bucket.tryAcquire()).isFalse();

            Thread.sleep(30);
            assertThat(bucket.tryAcquire()).isTrue();
        }

        @Test
        @DisplayName("never exceeds capacity")
        void neverExceedsCapacity() throws InterruptedException {
            RateLimitFilter.TokenBucket bucket = new RateLimitFilter.TokenBucket(2, 1000.0);
            Thread.sleep(30);
            assertThat(bucket.tryAcquire()).isTrue();
            assertThat(bucket.tryAcquire()).isTrue();
            assertThat(bucket.tryAcquire()).isFalse();
        }

        @Test
        @DisplayName("capacity 1 works correctly")
        void capacityOne() {
            RateLimitFilter.TokenBucket bucket = new RateLimitFilter.TokenBucket(1, 0.0);
            assertThat(bucket.tryAcquire()).isTrue();
            assertThat(bucket.tryAcquire()).isFalse();
        }
    }
}
