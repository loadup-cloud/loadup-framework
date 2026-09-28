package io.github.loadup.components.cache;

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
