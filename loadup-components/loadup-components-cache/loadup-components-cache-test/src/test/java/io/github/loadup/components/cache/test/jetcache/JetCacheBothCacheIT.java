package io.github.loadup.components.cache.test.jetcache;

/*-
 * #%L
 * Loadup Cache Component Test
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
