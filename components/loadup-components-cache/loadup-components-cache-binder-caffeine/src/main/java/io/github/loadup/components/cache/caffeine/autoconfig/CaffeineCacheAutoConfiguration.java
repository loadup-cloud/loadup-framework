package io.github.loadup.components.cache.caffeine.autoconfig;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.components.cache.binding.impl.DefaultCacheBinding;
import io.github.loadup.components.cache.caffeine.binder.CaffeineCacheBinder;
import io.github.loadup.components.cache.caffeine.cfg.CaffeineCacheBinderCfg;
import io.github.loadup.components.cache.cfg.CacheBindingCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(Caffeine.class)
public class CaffeineCacheAutoConfiguration {

    /**
     * 当 classpath 中存在 Caffeine 时，注册其元数据
     */
    @Bean
    public BindingMetadata<?, ?, ?, ?> caffeineMetadata() {
        return new BindingMetadata<>(
            "caffeine",
            DefaultCacheBinding.class,
            CaffeineCacheBinder.class,
            CacheBindingCfg.class,
            CaffeineCacheBinderCfg.class,
            ctx -> new DefaultCacheBinding());
    }
}
