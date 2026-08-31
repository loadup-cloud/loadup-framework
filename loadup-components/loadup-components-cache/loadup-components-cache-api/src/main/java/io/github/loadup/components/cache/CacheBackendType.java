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
