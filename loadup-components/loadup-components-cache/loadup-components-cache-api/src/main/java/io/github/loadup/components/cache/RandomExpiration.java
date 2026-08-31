package io.github.loadup.components.cache;

/*-
 * #%L
 * Loadup Cache Components API
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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
