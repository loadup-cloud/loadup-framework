package io.github.loadup.components.cache;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Shared anti-avalanche helper: every binder applies the same random TTL semantics so that a cache
 * name configured once behaves identically regardless of the underlying middleware.
 */
public final class RandomExpiration {

    private RandomExpiration() {}

    /**
     * Returns {@code base + random[0, range]}. When {@code range} is {@code null} or zero, the base
     * duration is returned unchanged.
     */
    public static Duration apply(Duration base, Duration range) {
        if (base == null) {
            return null;
        }
        if (range == null || range.isZero() || range.isNegative()) {
            return base;
        }
        long jitterNanos = ThreadLocalRandom.current().nextLong(range.toNanos() + 1);
        return base.plusNanos(jitterNanos);
    }
}
