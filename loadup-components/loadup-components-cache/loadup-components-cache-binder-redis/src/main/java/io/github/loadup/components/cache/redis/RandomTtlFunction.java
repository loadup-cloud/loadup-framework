package io.github.loadup.components.cache.redis;

/*-
 * #%L
 * Loadup Cache Binder Redis
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.cache.RandomExpiration;
import java.time.Duration;
import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * Spring Data Redis {@link RedisCacheWriter.TtlFunction} computing {@code ttl + random[0, range]}
 * per key on every write. Random expiration spreads cache expiry over time to avoid avalanche.
 */
public final class RandomTtlFunction implements RedisCacheWriter.TtlFunction {

    private final Duration base;
    private final Duration jitter;

    public RandomTtlFunction(Duration base, Duration jitter) {
        this.base = base;
        this.jitter = jitter;
    }

    @Override
    public Duration getTimeToLive(Object key, Object value) {
        Duration ttl = RandomExpiration.apply(base, jitter);
        return ttl == null ? NO_EXPIRATION : ttl;
    }
}
