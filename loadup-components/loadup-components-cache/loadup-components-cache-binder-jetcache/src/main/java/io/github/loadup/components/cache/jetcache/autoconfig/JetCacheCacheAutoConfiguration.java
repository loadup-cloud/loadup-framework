package io.github.loadup.components.cache.jetcache.autoconfig;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.redis.springdata.RedisSpringDataCacheBuilder;
import com.alicp.jetcache.template.CacheBuilderTemplate;
import io.github.loadup.components.cache.CacheBackendType;
import io.github.loadup.components.cache.LoadupCacheProperties;
import io.github.loadup.components.cache.autoconfig.LoadupCacheAutoConfiguration;
import io.github.loadup.components.cache.codec.CacheJsonCodec;
import io.github.loadup.components.cache.jetcache.JetCacheCacheProperties;
import io.github.loadup.components.cache.jetcache.JetCacheSpringCacheManager;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.data.redis.connection.RedisConnectionFactory;
import tools.jackson.databind.ObjectMapper;

/**
 * JetCache binder: adapts the Spring Cache facade to JetCache when
 * {@code loadup.cache.type=jetcache}. JetCache is wired programmatically (local Caffeine level plus
 * an optional Spring Data Redis remote level); the remote level appears only when a
 * {@link RedisConnectionFactory} exists, so LOCAL-only usage needs no Redis at all.
 */
@AutoConfiguration(after = LoadupCacheAutoConfiguration.class, before = CacheAutoConfiguration.class)
@ConditionalOnClass({SimpleCacheManager.class, CacheBuilderTemplate.class})
@ConditionalOnProperty(prefix = "loadup.cache", name = "type", havingValue = CacheBackendType.JETCACHE)
@EnableConfigurationProperties(JetCacheCacheProperties.class)
public class JetCacheCacheAutoConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(com.alicp.jetcache.CacheManager.class)
    public com.alicp.jetcache.CacheManager jetCacheBackend(
            JetCacheCacheProperties binderProperties,
            ObjectProvider<RedisConnectionFactory> connectionFactoryProvider) {
        Map<String, CacheBuilder> localBuilders = new HashMap<>();
        localBuilders.put(
                "default", CaffeineCacheBuilder.createCaffeineCacheBuilder().limit(binderProperties.getLocalLimit()));

        Map<String, CacheBuilder> remoteBuilders = new HashMap<>();
        RedisConnectionFactory connectionFactory = connectionFactoryProvider.getIfAvailable();
        if (connectionFactory != null) {
            remoteBuilders.put(
                    "default",
                    RedisSpringDataCacheBuilder.createBuilder()
                            .connectionFactory(connectionFactory)
                            .keyPrefixSupplier(() -> binderProperties.getRemoteKeyPrefix()));
        }

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCacheBuilderTemplate(
                new CacheBuilderTemplate(binderProperties.isPenetrationProtect(), localBuilders, remoteBuilders));
        return manager;
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(
            com.alicp.jetcache.CacheManager jetCacheBackend,
            LoadupCacheProperties properties,
            JetCacheCacheProperties binderProperties,
            ObjectProvider<RedisConnectionFactory> connectionFactoryProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider,
            ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        CacheJsonCodec codec = new CacheJsonCodec(objectMapper);

        JetCacheSpringCacheManager manager = new JetCacheSpringCacheManager(
                jetCacheBackend,
                properties,
                binderProperties,
                key -> "::" + String.valueOf(key),
                codec::serialize,
                codec::deserialize,
                connectionFactoryProvider.getIfAvailable(),
                binderProperties.getRemoteKeyPrefix());
        manager.preloadConfiguredCaches();
        return new CacheManagerCustomizers(customizers.orderedStream().toList()).customize(manager);
    }
}
