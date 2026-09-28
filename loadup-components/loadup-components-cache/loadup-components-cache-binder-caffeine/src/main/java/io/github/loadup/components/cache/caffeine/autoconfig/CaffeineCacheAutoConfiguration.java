package io.github.loadup.components.cache.caffeine.autoconfig;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.loadup.components.cache.CacheBackendType;
import io.github.loadup.components.cache.LoadupCacheProperties;
import io.github.loadup.components.cache.autoconfig.LoadupCacheAutoConfiguration;
import io.github.loadup.components.cache.caffeine.CaffeineCacheProperties;
import io.github.loadup.components.cache.caffeine.LoadupCaffeineCacheManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizers;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;

/**
 * Caffeine binder: provides the Spring {@link CacheManager} when {@code loadup.cache.type=caffeine}
 * (the default). Business code only sees the Spring Cache facade.
 */
@AutoConfiguration(after = LoadupCacheAutoConfiguration.class, before = CacheAutoConfiguration.class)
@ConditionalOnClass(Caffeine.class)
@ConditionalOnProperty(
        prefix = "loadup.cache",
        name = "type",
        havingValue = CacheBackendType.CAFFEINE,
        matchIfMissing = true)
@EnableConfigurationProperties(CaffeineCacheProperties.class)
public class CaffeineCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(
            LoadupCacheProperties properties,
            CaffeineCacheProperties binderProperties,
            ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        LoadupCaffeineCacheManager manager =
                new LoadupCaffeineCacheManager(properties, binderProperties.getMaximumSize());
        return new CacheManagerCustomizers(customizers.orderedStream().toList()).customize(manager);
    }
}
