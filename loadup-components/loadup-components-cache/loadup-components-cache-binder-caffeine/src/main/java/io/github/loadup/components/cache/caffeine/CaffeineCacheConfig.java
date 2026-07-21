package io.github.loadup.components.cache.caffeine;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.cache.binder.caffeine")
public class CaffeineCacheConfig {
    private long maximumSize = 10000;
    private Duration expireAfterWrite = Duration.ofMinutes(10);
    private Duration expireAfterAccess;

    public long getMaximumSize() { return maximumSize; }
    public void setMaximumSize(long maximumSize) { this.maximumSize = maximumSize; }
    public Duration getExpireAfterWrite() { return expireAfterWrite; }
    public void setExpireAfterWrite(Duration expireAfterWrite) { this.expireAfterWrite = expireAfterWrite; }
    public Duration getExpireAfterAccess() { return expireAfterAccess; }
    public void setExpireAfterAccess(Duration expireAfterAccess) { this.expireAfterAccess = expireAfterAccess; }
}
