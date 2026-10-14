/*
 * #%L
 * Loadup Cache Test
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
package io.github.loadup.components.cache.test;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.cache.CacheBackendType;
import io.github.loadup.components.cache.CacheNameSettings;
import io.github.loadup.components.cache.LoadupCacheProperties;
import io.github.loadup.components.cache.RandomExpiration;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CachePropertiesContractTest {
    @Test
    void perNamePoliciesOverrideDefaultsAndUnconfiguredFieldsFallBack() {
        LoadupCacheProperties properties = new LoadupCacheProperties();
        assertThat(properties.getType()).isEqualTo(CacheBackendType.CAFFEINE);
        assertThat(properties.getDefaultAllowNullValues()).isTrue();
        assertThat(properties.getCaches()).isEmpty();
        properties.setType(CacheBackendType.REDIS);
        properties.setDefaultTtl(Duration.ofMinutes(5));
        properties.setDefaultAllowNullValues(false);
        CacheNameSettings explicit = new CacheNameSettings(Duration.ofSeconds(30), Duration.ofSeconds(3), true);
        properties.setCaches(Map.of("explicit", explicit, "inherited", new CacheNameSettings(null, null, null)));
        assertThat(properties.getType()).isEqualTo(CacheBackendType.REDIS);
        assertThat(properties.getDefaultTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(properties.getDefaultAllowNullValues()).isFalse();
        assertThat(properties.cacheSettings("explicit")).isSameAs(explicit);
        assertThat(properties.ttl("explicit", properties.getDefaultTtl())).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.ttl("inherited", properties.getDefaultTtl())).isEqualTo(Duration.ofMinutes(5));
        assertThat(properties.ttl("unknown", properties.getDefaultTtl())).isEqualTo(Duration.ofMinutes(5));
        assertThat(properties.randomExpirationRange("explicit")).isEqualTo(Duration.ofSeconds(3));
        assertThat(properties.randomExpirationRange("inherited")).isNull();
        assertThat(properties.randomExpirationRange("unknown")).isNull();
        assertThat(properties.allowNullValues("explicit")).isTrue();
        assertThat(properties.allowNullValues("inherited")).isFalse();
        assertThat(properties.allowNullValues("unknown")).isFalse();
    }

    @Test
    void explicitNullRejectionOverridesDefaultAndAbsentGlobalPolicyRejectsNulls() {
        LoadupCacheProperties properties = new LoadupCacheProperties();
        properties.setCaches(Map.of("private", new CacheNameSettings(null, null, false)));
        assertThat(properties.allowNullValues("private")).isFalse();
        assertThat(properties.allowNullValues("public")).isTrue();
        properties.setDefaultAllowNullValues(null);
        assertThat(properties.allowNullValues("public")).isFalse();
    }

    @Test
    void negativeJitterCannotShortenAnEntryLifetime() {
        Duration base = Duration.ofSeconds(10);
        assertThat(RandomExpiration.apply(base, Duration.ofSeconds(-2))).isEqualTo(base);
    }
}
