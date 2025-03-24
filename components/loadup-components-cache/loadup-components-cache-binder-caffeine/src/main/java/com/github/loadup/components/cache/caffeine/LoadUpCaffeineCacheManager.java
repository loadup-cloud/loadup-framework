package com.github.loadup.components.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class LoadUpCaffeineCacheManager  extends CaffeineCacheManager {

    @Override
    protected Cache createCaffeineCache(String name) {
        String[] array = StringUtils.delimitedListToStringArray(name, "#");
        name = array[0];
        // 解析TTL
        if (array.length > 1) {
            String duration = array[1];
            if (!StringUtils.startsWithIgnoreCase(duration, "PT")) {
                duration = "PT" + duration;
            }
        }
        Cache caffeineCache = super.createCaffeineCache(name);
        return caffeineCache;
    }
}
