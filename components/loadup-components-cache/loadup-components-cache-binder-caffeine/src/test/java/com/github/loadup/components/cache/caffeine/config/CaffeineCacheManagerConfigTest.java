package com.github.loadup.components.cache.caffeine.config;

/*-
 * #%L
 * loadup-components-cache-binder-caffeine
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

/** Test for LoadUpCaffeineCacheManager configuration override functionality */
@SpringBootTest
@TestPropertySource(
    properties = {
      "loadup.cache.binder=caffeine",
      // Global default configuration - applies to all caches
      "spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=5s",
      // Per-cache custom configuration - overrides global default
      "loadup.cache.cache-configs.customCache.expire-after-write=10s",
      "loadup.cache.cache-configs.customCache.maximum-size=200"
    })
@DisplayName("Caffeine Cache Manager Configuration Override Test")
public class CaffeineCacheManagerConfigTest {

  @Autowired private CacheManager cacheManager;

  @Test
  @DisplayName("测试 CacheManager 正确注入")
  void testCacheManagerInjected() {
    assertNotNull(cacheManager, "CacheManager should be injected");
    assertInstanceOf(
        LoadUpCaffeineCacheManager.class,
        cacheManager,
        "CacheManager should be instance of LoadUpCaffeineCacheManager");
  }

  @Test
  @DisplayName("测试使用默认配置创建缓存")
  void testCreateCacheWithDefaultConfig() {
    // This cache is not in cache-configs, so it should use spring.cache.caffeine.spec
    var cache = cacheManager.getCache("defaultCache");
    assertNotNull(cache, "Default cache should be created");

    // Verify cache works
    cache.put("key1", "value1");
    assertEquals("value1", cache.get("key1", String.class));
  }

  @Test
  @DisplayName("测试使用自定义配置创建缓存")
  void testCreateCacheWithCustomConfig() {
    // This cache has custom config in cache-configs, should override default
    var cache = cacheManager.getCache("customCache");
    assertNotNull(cache, "Custom cache should be created");

    // Verify cache works
    cache.put("key1", "value1");
    assertEquals("value1", cache.get("key1", String.class));
  }

  @Test
  @DisplayName("测试自定义配置的缓存规格")
  void testCustomCacheSpecs() {
    LoadUpCaffeineCacheManager manager = (LoadUpCaffeineCacheManager) cacheManager;

    // Verify custom cache configuration is registered
    var customSpecs = manager.getCustomCaffeineSpecs();
    assertTrue(
        customSpecs.containsKey("customCache"), "customCache should have custom configuration");
  }

  @Test
  @DisplayName("测试多个缓存可以共存")
  void testMultipleCachesCoexist() {
    var defaultCache = cacheManager.getCache("defaultCache");
    var customCache = cacheManager.getCache("customCache");
    var anotherCache = cacheManager.getCache("anotherCache");

    assertNotNull(defaultCache);
    assertNotNull(customCache);
    assertNotNull(anotherCache);

    // Each cache should work independently
    defaultCache.put("key", "default");
    customCache.put("key", "custom");
    anotherCache.put("key", "another");

    assertEquals("default", defaultCache.get("key", String.class));
    assertEquals("custom", customCache.get("key", String.class));
    assertEquals("another", anotherCache.get("key", String.class));
  }
}
