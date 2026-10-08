package io.github.loadup.components.cache.autoconfig;

import io.github.loadup.components.cache.LoadupCacheProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;

/**
 * LoadUp cache facade auto-configuration.
 *
 * <p>Enables Spring Cache annotation-driven caching so that business code can start using
 * {@code @Cacheable} right after adding a binder dependency. A matching binder auto-configuration
 * (caffeine / redis / jetcache) supplies the actual {@link CacheManager}; without any binder Spring
 * Boot falls back to a no-op manager.
 */
@AutoConfiguration(before = CacheAutoConfiguration.class)
@ConditionalOnClass(CacheManager.class)
@EnableCaching
@EnableConfigurationProperties(LoadupCacheProperties.class)
public class LoadupCacheAutoConfiguration {}
