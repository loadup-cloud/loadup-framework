package io.github.loadup.components.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.cache")
public class CacheProperties {
    private String binderType = "caffeine";
    private String keyPrefix = "";

    public String getBinderType() { return binderType; }
    public void setBinderType(String binderType) { this.binderType = binderType; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
}
