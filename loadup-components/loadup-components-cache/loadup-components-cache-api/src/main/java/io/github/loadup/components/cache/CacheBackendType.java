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

/**
 * Values for {@code loadup.cache.type}.
 *
 * <p>The type must match the binder jar that is present on the classpath. Switching the backend
 * means changing this property (and the binder dependency) without touching business code.
 */
public final class CacheBackendType {

    /** Local in-process cache backed by Caffeine. Default when no explicit type is set. */
    public static final String CAFFEINE = "caffeine";

    /** Distributed cache backed by Spring Data Redis. */
    public static final String REDIS = "redis";

    /** JetCache multi-level cache (local + remote, optional local sync). */
    public static final String JETCACHE = "jetcache";

    /** Disables all caching; {@code @Cacheable} annotations become no-ops. */
    public static final String NONE = "none";

    private CacheBackendType() {}
}
