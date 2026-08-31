package io.github.loadup.components.cache.redis;

/*-
 * #%L
 * Loadup Cache Binder Redis
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
