package io.github.loadup.components.cache.test.jetcache;

import io.github.loadup.components.cache.jetcache.JetCacheSpringCacheManager;
import io.github.loadup.components.cache.test.AbstractCacheBinderIT;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import org.springframework.test.context.TestPropertySource;

/** Spring Cache facade on JetCache multi-level layout (local Caffeine + remote Redis). */
@EnableTestContainers(ContainerType.REDIS)
@TestPropertySource(
        properties = {
            "loadup.cache.type=jetcache",
            "loadup.cache.binder.jetcache.default-cache-type=BOTH",
        })
class JetCacheBothCacheIT extends AbstractCacheBinderIT {

    @Override
    protected Class<?> expectedCacheManagerType() {
        return JetCacheSpringCacheManager.class;
    }
}
