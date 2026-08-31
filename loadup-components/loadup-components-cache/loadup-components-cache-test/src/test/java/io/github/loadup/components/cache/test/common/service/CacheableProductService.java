package io.github.loadup.components.cache.test.common.service;

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

import io.github.loadup.components.cache.test.common.model.Product;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Plain Spring Cache business service. It never imports a LoadUp cache class: this is the contract
 * that keeps the same business code running on caffeine, redis or jetcache.
 */
@Service
public class CacheableProductService {

    private final AtomicInteger productLoadCount = new AtomicInteger();
    private final AtomicInteger shortTtlLoadCount = new AtomicInteger();
    private final AtomicInteger jitterLoadCount = new AtomicInteger();
    private final AtomicInteger nullableLoadCount = new AtomicInteger();

    @Cacheable(cacheNames = "product", key = "#id")
    public Product getProduct(Long id) {
        productLoadCount.incrementAndGet();
        return new Product(id, "product-" + id);
    }

    @CachePut(cacheNames = "product", key = "#id")
    public Product updateProduct(Long id, String name) {
        return new Product(id, name);
    }

    @CacheEvict(cacheNames = "product", key = "#id")
    public void deleteProduct(Long id) {}

    @CacheEvict(cacheNames = "product", allEntries = true)
    public void clearProducts() {}

    @Cacheable(cacheNames = "productShortTtl", key = "#id")
    public Product getShortLivedProduct(Long id) {
        shortTtlLoadCount.incrementAndGet();
        return new Product(id, "short-" + id);
    }

    @Cacheable(cacheNames = "productJitter", key = "#id")
    public Product getJitterProduct(Long id) {
        jitterLoadCount.incrementAndGet();
        return new Product(id, "jitter-" + id);
    }

    @Cacheable(cacheNames = "productNullable", key = "#id")
    public Product getNullableProduct(Long id) {
        nullableLoadCount.incrementAndGet();
        return null;
    }

    public void resetCounters() {
        productLoadCount.set(0);
        shortTtlLoadCount.set(0);
        jitterLoadCount.set(0);
        nullableLoadCount.set(0);
    }

    public int getProductLoadCount() {
        return productLoadCount.get();
    }

    public int getShortTtlLoadCount() {
        return shortTtlLoadCount.get();
    }

    public int getJitterLoadCount() {
        return jitterLoadCount.get();
    }

    public int getNullableLoadCount() {
        return nullableLoadCount.get();
    }
}
