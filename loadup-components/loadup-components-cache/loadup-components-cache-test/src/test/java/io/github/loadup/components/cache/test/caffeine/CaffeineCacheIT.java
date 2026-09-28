package io.github.loadup.components.cache.test.caffeine;

import io.github.loadup.components.cache.caffeine.LoadupCaffeineCacheManager;
import io.github.loadup.components.cache.test.AbstractCacheBinderIT;
import org.springframework.test.context.TestPropertySource;

/** Spring Cache facade on the default local Caffeine binder. */
@TestPropertySource(properties = "loadup.cache.type=caffeine")
class CaffeineCacheIT extends AbstractCacheBinderIT {

    @Override
    protected Class<?> expectedCacheManagerType() {
        return LoadupCaffeineCacheManager.class;
    }
}
