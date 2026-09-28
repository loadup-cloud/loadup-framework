package io.github.loadup.components.cache.caffeine;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Caffeine binder settings ({@code loadup.cache.binder.caffeine.*}). */
@ConfigurationProperties(prefix = "loadup.cache.binder.caffeine")
public class CaffeineCacheProperties {

    /** Maximum number of entries per cache before eviction. */
    private long maximumSize = 10_000;

    public long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }
}
