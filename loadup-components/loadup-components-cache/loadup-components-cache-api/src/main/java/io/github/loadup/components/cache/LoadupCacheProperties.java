package io.github.loadup.components.cache;

/*-
 * #%L
 * Loadup Cache Components API
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

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LoadUp cache facade configuration ({@code loadup.cache.*}).
 *
 * <p>The facade is Spring Cache itself: business code uses {@code @Cacheable} /
 * {@code @CacheEvict} / {@code @CachePut} and never imports a LoadUp cache class. This class only
 * carries the LoadUp additions that Spring Cache cannot express: per-cache TTL, null caching and
 * random expiration (anti-avalanche).
 */
@ConfigurationProperties(prefix = "loadup.cache")
public class LoadupCacheProperties {

    /** Backend selector, one of {@link CacheBackendType}. */
    private String type = CacheBackendType.CAFFEINE;

    /** Default TTL for caches without a per-name setting. {@code null} means no expiration. */
    private Duration defaultTtl;

    /** Whether null return values may be cached when a cache has no per-name setting. */
    private Boolean defaultAllowNullValues = true;

    /** Per-cache-name settings keyed by cache name. */
    private Map<String, CacheNameSettings> caches = new LinkedHashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Duration getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    public Boolean getDefaultAllowNullValues() {
        return defaultAllowNullValues;
    }

    public void setDefaultAllowNullValues(Boolean defaultAllowNullValues) {
        this.defaultAllowNullValues = defaultAllowNullValues;
    }

    public Map<String, CacheNameSettings> getCaches() {
        return caches;
    }

    public void setCaches(Map<String, CacheNameSettings> caches) {
        this.caches = caches;
    }

    public CacheNameSettings cacheSettings(String cacheName) {
        return caches.get(cacheName);
    }

    public Duration ttl(String cacheName, Duration fallback) {
        CacheNameSettings settings = cacheSettings(cacheName);
        return settings != null && settings.ttl() != null ? settings.ttl() : fallback;
    }

    public Duration randomExpirationRange(String cacheName) {
        CacheNameSettings settings = cacheSettings(cacheName);
        return settings != null ? settings.randomExpirationRange() : null;
    }

    public boolean allowNullValues(String cacheName) {
        CacheNameSettings settings = cacheSettings(cacheName);
        if (settings != null && settings.allowNullValues() != null) {
            return settings.allowNullValues();
        }
        return Boolean.TRUE.equals(defaultAllowNullValues);
    }
}
