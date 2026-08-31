package io.github.loadup.components.cache.test.caffeine;

/*-
 * #%L
 * Loadup Cache Component Test
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
