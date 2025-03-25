package com.github.loadup.components.cache.redis;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;

public class LoadUpRedisCacheManager extends RedisCacheManager {
    private final RedisCacheConfiguration defaultConfig;

    public LoadUpRedisCacheManager(
            RedisCacheWriter cacheWriter,
            RedisCacheConfiguration defaultCacheConfiguration,
            Map<String, RedisCacheConfiguration> initialCacheConfigurations) {
        super(cacheWriter, defaultCacheConfiguration, initialCacheConfigurations);
        this.defaultConfig = defaultCacheConfiguration;
    }

    /**
     * name#ttl
     * test#60S
     * key#60M
     * @return
     */
    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        // 在动态创建缓存时，复用默认配置
        if (cacheConfig == null) {
            cacheConfig = defaultConfig;
        }

        String[] array = StringUtils.delimitedListToStringArray(name, "#");
        name = array[0];
        // 解析TTL
        if (array.length > 1) {
            String duration = array[1];
            if (!StringUtils.startsWithIgnoreCase(duration, "PT")) {
                duration = "PT" + duration;
            }
            cacheConfig = cacheConfig.entryTtl(Duration.parse(duration));
        }
        return super.createRedisCache(name, cacheConfig);
    }
}
