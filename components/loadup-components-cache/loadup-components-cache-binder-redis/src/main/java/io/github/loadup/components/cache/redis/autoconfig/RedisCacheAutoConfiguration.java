package io.github.loadup.components.cache.redis.autoconfig;

import io.github.loadup.components.cache.binding.impl.DefaultCacheBinding;
import io.github.loadup.components.cache.cfg.CacheBindingCfg;
import io.github.loadup.components.cache.redis.binder.RedisCacheBinder;
import io.github.loadup.components.cache.redis.cfg.RedisCacheBinderCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(RedisCacheBinder.class)
public class RedisCacheAutoConfiguration {

    /**
     * 当 classpath 中存在 Redis 时，注册其元数据
     */
    @Bean
    public BindingMetadata<?, ?, ?, ?> redisMetadata() {
        return new BindingMetadata<>(
            "redis",
            DefaultCacheBinding.class,
            RedisCacheBinder.class,
            CacheBindingCfg.class,
            RedisCacheBinderCfg.class,
            ctx -> new DefaultCacheBinding());
    }
}
