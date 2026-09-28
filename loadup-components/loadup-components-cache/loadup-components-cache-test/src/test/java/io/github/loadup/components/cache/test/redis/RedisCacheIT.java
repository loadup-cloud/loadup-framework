package io.github.loadup.components.cache.test.redis;

import io.github.loadup.components.cache.test.AbstractCacheBinderIT;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.TestPropertySource;

/** Spring Cache facade on Spring Data Redis (Testcontainers). */
@EnableTestContainers(ContainerType.REDIS)
@TestPropertySource(properties = "loadup.cache.type=redis")
class RedisCacheIT extends AbstractCacheBinderIT {

    @Override
    protected Class<?> expectedCacheManagerType() {
        return RedisCacheManager.class;
    }
}
