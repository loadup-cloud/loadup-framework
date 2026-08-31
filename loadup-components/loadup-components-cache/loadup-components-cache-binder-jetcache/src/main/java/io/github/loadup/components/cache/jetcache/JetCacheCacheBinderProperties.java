package io.github.loadup.components.cache.jetcache;

/*-
 * #%L
 * Loadup Cache Binder JetCache
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
