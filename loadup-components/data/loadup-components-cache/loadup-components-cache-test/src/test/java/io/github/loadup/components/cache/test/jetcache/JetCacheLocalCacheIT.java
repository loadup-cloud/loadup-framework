package io.github.loadup.components.cache.test.jetcache;

import io.github.loadup.components.cache.jetcache.JetCacheSpringCacheManager;
import io.github.loadup.components.cache.test.AbstractCacheBinderIT;
import org.springframework.test.context.TestPropertySource;

/** Spring Cache facade on JetCache with a local-only layout (no Redis required). */
@TestPropertySource(
        properties = {
            "loadup.cache.type=jetcache",
            "loadup.cache.binder.jetcache.default-cache-type=LOCAL",
        })
class JetCacheLocalCacheIT extends AbstractCacheBinderIT {

    @Override
    protected Class<?> expectedCacheManagerType() {
        return JetCacheSpringCacheManager.class;
    }
}
