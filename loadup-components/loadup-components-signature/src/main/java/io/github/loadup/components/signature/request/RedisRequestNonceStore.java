/*
 * #%L
 * LoadUp Components Signature
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
package io.github.loadup.components.signature.request;

import java.time.Duration;
import java.util.Objects;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Uses one SET NX with an expiry; never releases claims after business failure. */
public final class RedisRequestNonceStore implements RequestNonceStore {
    private final StringRedisTemplate redis;

    public RedisRequestNonceStore(StringRedisTemplate redis) {
        this.redis = Objects.requireNonNull(redis);
    }

    @Override
    public boolean claim(String replayKey, Duration ttl) {
        if (replayKey == null
                || !replayKey.matches("[a-f0-9]{64}")
                || ttl == null
                || ttl.isNegative()
                || ttl.isZero()) {
            throw new IllegalArgumentException("Invalid nonce claim");
        }
        long millis = ttl.toMillis();
        if (!ttl.equals(Duration.ofMillis(millis))) millis++;
        Boolean result = redis.opsForValue()
                .setIfAbsent("loadup:signature:nonce:" + replayKey, "1", Duration.ofMillis(Math.max(1, millis)));
        if (result == null) throw new IllegalStateException("Nonce claim requires an immediate Redis result");
        return result;
    }
}
