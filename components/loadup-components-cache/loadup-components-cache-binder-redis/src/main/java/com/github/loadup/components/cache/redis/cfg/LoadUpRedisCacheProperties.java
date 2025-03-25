package com.github.loadup.components.cache.redis.cfg;

import com.github.loadup.components.cache.cfg.LoadUpCacheConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.cache.redis")
public class LoadUpRedisCacheProperties {
    // 自定义缓存过期配置
    private Map<String, LoadUpCacheConfig> cacheConfig = new HashMap<>();
}
