package io.github.loadup.components.cache.jetcache;

/**
 * Per-cache JetCache settings ({@code loadup.cache.binder.jetcache.caches.<name>.*}).
 *
 * @param cacheType LOCAL, REMOTE or BOTH; overrides the default cache type
 * @param localLimit maximum entries of the local level; overrides the default local limit
 */
public record JetCacheCacheSettings(String cacheType, Integer localLimit) {}
