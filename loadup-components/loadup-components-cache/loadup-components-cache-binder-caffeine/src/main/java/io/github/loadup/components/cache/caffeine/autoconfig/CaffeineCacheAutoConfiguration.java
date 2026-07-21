package io.github.loadup.components.cache.caffeine.autoconfig;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.components.cache.CacheProvider;
import io.github.loadup.components.cache.caffeine.CaffeineCacheConfig;
import io.github.loadup.components.cache.caffeine.CaffeineCacheProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(Caffeine.class)
@ConditionalOnProperty(prefix = "loadup.cache", name = "binder-type", havingValue = "caffeine", matchIfMissing = true)
@EnableConfigurationProperties(CaffeineCacheConfig.class)
public class CaffeineCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheProvider caffeineCacheProvider(CaffeineCacheConfig config) {
        return new CaffeineCacheProvider(config);
    }
}
