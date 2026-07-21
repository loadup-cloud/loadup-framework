package io.github.loadup.components.cache.redis.autoconfig;

import io.github.loadup.components.cache.CacheProvider;
import io.github.loadup.components.cache.redis.RedisCacheConfig;
import io.github.loadup.components.cache.redis.RedisCacheProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.ByteArrayRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@AutoConfiguration
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty(prefix = "loadup.cache", name = "binder-type", havingValue = "redis")
@EnableConfigurationProperties(RedisCacheConfig.class)
public class RedisCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, byte[]> cacheRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(StringRedisSerializer.UTF_8);
        template.setValueSerializer(ByteArrayRedisSerializer.INSTANCE);
        template.setHashKeySerializer(StringRedisSerializer.UTF_8);
        template.setHashValueSerializer(ByteArrayRedisSerializer.INSTANCE);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheProvider redisCacheProvider(RedisTemplate<String, byte[]> redisTemplate) {
        return new RedisCacheProvider(redisTemplate);
    }
}
