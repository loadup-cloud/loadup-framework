package io.github.loadup.components.cache;

import java.time.Duration;

/**
 * Per-cache-name facade settings shared by every binder.
 *
 * <p>Each field is optional; unset fields fall back to {@link LoadupCacheProperties} defaults.
 * Binder-specific capabilities (e.g. JetCache multi-level layout) live in the binder module.
 */
public record CacheNameSettings(Duration ttl, Duration randomExpirationRange, Boolean allowNullValues) {}
