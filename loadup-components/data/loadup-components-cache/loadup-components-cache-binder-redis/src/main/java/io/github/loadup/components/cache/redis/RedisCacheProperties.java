package io.github.loadup.components.cache.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Redis binder settings ({@code loadup.cache.binder.redis.*}). */
@ConfigurationProperties(prefix = "loadup.cache.binder.redis")
public class RedisCacheProperties {

    /** Key prefix applied to every cache name (Redis keys look like {@code <prefix><name>::<key>}). */
    private String keyPrefix = "loadup:cache:";

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}
