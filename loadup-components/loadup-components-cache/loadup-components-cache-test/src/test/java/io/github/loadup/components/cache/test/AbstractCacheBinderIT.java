package io.github.loadup.components.cache.test;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.loadup.components.cache.test.common.model.Product;
import io.github.loadup.components.cache.test.common.service.CacheableProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Binder-independent contract: the same business service and the same facade configuration must
 * behave identically on caffeine, redis and jetcache. Each binder subclass only changes
 * {@code loadup.cache.type} (plus binder-specific layout for jetcache).
 */
@SpringBootTest(classes = TestCacheApplication.class)
@ActiveProfiles("test")
public abstract class AbstractCacheBinderIT {

    @Autowired
    protected CacheableProductService productService;

    @Autowired
    protected CacheManager cacheManager;

    /** Expected {@link CacheManager} implementation, proving the right binder was selected. */
    protected abstract Class<?> expectedCacheManagerType();

    @BeforeEach
    void setUp() {
        productService.resetCounters();
        cacheManager.getCache("product").clear();
        cacheManager.getCache("productShortTtl").clear();
        cacheManager.getCache("productJitter").clear();
        cacheManager.getCache("productNullable").clear();
    }

    @Test
    void configuredBinderIsActive() {
        assertInstanceOf(expectedCacheManagerType(), cacheManager);
    }

    @Test
    void cacheableCachesSecondInvocation() {
        Product first = productService.getProduct(1L);
        Product second = productService.getProduct(1L);

        assertEquals(1, productService.getProductLoadCount());
        assertNotNull(first);
        assertEquals(first, second);
    }

    @Test
    void cachePutRefreshesEntry() {
        productService.getProduct(2L);
        productService.updateProduct(2L, "updated");
        Product cached = productService.getProduct(2L);

        assertEquals(1, productService.getProductLoadCount());
        assertEquals("updated", cached.name());
    }

    @Test
    void cacheEvictRemovesEntry() {
        productService.getProduct(3L);
        productService.deleteProduct(3L);
        productService.getProduct(3L);

        assertEquals(2, productService.getProductLoadCount());
    }

    @Test
    void cacheEvictAllEntriesClearsCache() {
        productService.getProduct(4L);
        productService.getProduct(5L);
        productService.clearProducts();
        productService.getProduct(4L);
        productService.getProduct(5L);

        assertEquals(4, productService.getProductLoadCount());
    }

    @Test
    void nullValueIsCached() {
        assertNull(productService.getNullableProduct(6L));
        assertNull(productService.getNullableProduct(6L));

        assertEquals(1, productService.getNullableLoadCount());
    }

    @Test
    void ttlExpiresEntries() throws InterruptedException {
        productService.getShortLivedProduct(7L);
        assertEquals(1, productService.getShortTtlLoadCount());

        Thread.sleep(900);

        productService.getShortLivedProduct(7L);
        assertEquals(2, productService.getShortTtlLoadCount());
    }

    @Test
    void randomExpirationStillExpiresEntries() throws InterruptedException {
        productService.getJitterProduct(8L);
        productService.getJitterProduct(9L);
        assertEquals(2, productService.getJitterLoadCount());

        Thread.sleep(1500);

        productService.getJitterProduct(8L);
        productService.getJitterProduct(9L);
        assertEquals(4, productService.getJitterLoadCount());
    }
}
