package io.github.loadup.components.cache.jetcache;

/*-
 * #%L
 * Loadup Cache Binder JetCache
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

import com.alicp.jetcache.anno.CacheType;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JetCache binder settings ({@code loadup.cache.binder.jetcache.*}).
 *
 * <p>These are binder-specific capabilities that the shared Spring Cache facade cannot express;
 * common settings (TTL, null values, random expiration) stay under {@code loadup.cache.*}.
 */
@ConfigurationProperties(prefix = "loadup.cache.binder.jetcache")
public class JetCacheCacheBinderProperties {

    /** Default layout for caches without a per-name setting: LOCAL, REMOTE or BOTH. */
    private String defaultCacheType = CacheType.LOCAL.name();

    /** Maximum entries of the local level before eviction. */
    private int localLimit = 10_000;

    /** Push local-level invalidations through the remote channel (requires REMOTE or BOTH). */
    private boolean syncLocal = true;

    /** Protect against cache penetration (concurrent loads of the same key are serialized). */
    private boolean penetrationProtect = false;

    /** Key prefix for the remote level; the cache name and a {@code ::} separator are appended. */
    private String remoteKeyPrefix = "loadup:cache:";

    /** Per-cache overrides keyed by cache name. */
    private Map<String, JetCacheCacheSettings> caches = new LinkedHashMap<>();

    public String getDefaultCacheType() {
        return defaultCacheType;
    }

    public void setDefaultCacheType(String defaultCacheType) {
        this.defaultCacheType = defaultCacheType;
    }

    public int getLocalLimit() {
        return localLimit;
    }

    public void setLocalLimit(int localLimit) {
        this.localLimit = localLimit;
    }

    public boolean isSyncLocal() {
        return syncLocal;
    }

    public void setSyncLocal(boolean syncLocal) {
        this.syncLocal = syncLocal;
    }

    public boolean isPenetrationProtect() {
        return penetrationProtect;
    }

    public void setPenetrationProtect(boolean penetrationProtect) {
        this.penetrationProtect = penetrationProtect;
    }

    public String getRemoteKeyPrefix() {
        return remoteKeyPrefix;
    }

    public void setRemoteKeyPrefix(String remoteKeyPrefix) {
        this.remoteKeyPrefix = remoteKeyPrefix;
    }

    public Map<String, JetCacheCacheSettings> getCaches() {
        return caches;
    }

    public void setCaches(Map<String, JetCacheCacheSettings> caches) {
        this.caches = caches;
    }

    public CacheType cacheType(String cacheName) {
        JetCacheCacheSettings settings = caches.get(cacheName);
        String type = settings != null && settings.cacheType() != null ? settings.cacheType() : defaultCacheType;
        return CacheType.valueOf(type.toUpperCase(Locale.ROOT));
    }

    public Integer localLimit(String cacheName) {
        JetCacheCacheSettings settings = caches.get(cacheName);
        return settings != null && settings.localLimit() != null ? settings.localLimit() : localLimit;
    }
}
