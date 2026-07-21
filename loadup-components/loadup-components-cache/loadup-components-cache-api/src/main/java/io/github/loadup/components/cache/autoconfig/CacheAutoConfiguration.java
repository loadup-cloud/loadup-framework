package io.github.loadup.components.cache.autoconfig;

import io.github.loadup.components.cache.CacheProperties;
import io.github.loadup.components.cache.CacheProvider;
import io.github.loadup.components.cache.CacheTemplate;
import io.github.loadup.components.cache.DefaultCacheTemplate;
import io.github.loadup.components.cache.serializer.CacheSerializer;
import io.github.loadup.components.cache.serializer.JsonCacheSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnSingleCandidate(CacheProvider.class)
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CacheSerializer.class)
    public CacheSerializer cacheSerializer() {
        return new JsonCacheSerializer();
    }

    @Bean
    @ConditionalOnMissingBean(CacheTemplate.class)
    public CacheTemplate cacheTemplate(CacheProvider provider, CacheSerializer serializer, CacheProperties props) {
        return new DefaultCacheTemplate(provider, serializer, props.getKeyPrefix());
    }
}
