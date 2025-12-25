/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.caffeine;

/*-
 * #%L
 * loadup-components-cache-binder-caffeine
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extended Caffeine Cache Manager with per-cache custom configurations
 */
public class LoadUpCaffeineCacheManager extends CaffeineCacheManager {

    private final Map<String, Caffeine<Object, Object>> customCaffeineSpecs = new ConcurrentHashMap<>();

    /**
     * Register a custom Caffeine configuration for a specific cache
     *
     * @param cacheName Cache name
     * @param caffeine  Custom Caffeine builder
     */
    public void registerCustomCache(String cacheName, Caffeine<Object, Object> caffeine) {
        customCaffeineSpecs.put(cacheName, caffeine);
    }

    @Override
    protected Cache createCaffeineCache(String name) {
        // Check if there's a custom configuration for this cache
        Caffeine<Object, Object> customCaffeine = customCaffeineSpecs.get(name);

        if (customCaffeine != null) {
            // Use custom configuration
            return new CaffeineCache(name, customCaffeine.build(), isAllowNullValues());
        }

        // Fall back to default configuration
        return super.createCaffeineCache(name);
    }
}

