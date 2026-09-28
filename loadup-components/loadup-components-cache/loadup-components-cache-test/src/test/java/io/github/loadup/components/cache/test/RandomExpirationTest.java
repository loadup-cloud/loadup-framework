package io.github.loadup.components.cache.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.loadup.components.cache.RandomExpiration;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class RandomExpirationTest {

    @Test
    void returnsBaseWhenJitterIsNullOrZero() {
        Duration base = Duration.ofSeconds(30);

        assertEquals(base, RandomExpiration.apply(base, null));
        assertEquals(base, RandomExpiration.apply(base, Duration.ZERO));
    }

    @Test
    void returnsNullWhenBaseIsNull() {
        assertNull(RandomExpiration.apply(null, Duration.ofSeconds(5)));
    }

    @Test
    void returnsValueWithinBaseAndBasePlusJitter() {
        Duration base = Duration.ofSeconds(10);
        Duration jitter = Duration.ofSeconds(3);

        for (int i = 0; i < 100; i++) {
            Duration result = RandomExpiration.apply(base, jitter);
            assertTrue(!result.isNegative() && result.compareTo(base) >= 0);
            assertTrue(result.compareTo(base.plus(jitter)) <= 0);
        }
    }
}
