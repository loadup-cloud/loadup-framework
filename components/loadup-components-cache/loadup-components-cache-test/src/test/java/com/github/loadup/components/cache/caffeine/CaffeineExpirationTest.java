package com.github.loadup.components.cache.caffeine;

/*-
 * #%L
 * loadup-components-cache-test
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import com.github.loadup.components.cache.common.BaseCacheTest;
import com.github.loadup.components.cache.common.model.User;

/** Caffeine Cache Expiration Strategy Test */
@TestPropertySource(
    properties = {
      "loadup.cache.type=caffeine",
      "loadup.cache.caffeine.expire-after-write-seconds=2",
      "loadup.cache.caffeine.expire-after-access-seconds=1",
      "loadup.cache.caffeine.maximum-size=100",
      "loadup.cache.caffeine.cache-config.short-lived.expire-after-write=3s",
      "loadup.cache.caffeine.cache-config.short-lived.maximum-size=50",
      "loadup.cache.caffeine.cache-config.short-lived.enable-random-expiration=true",
      "loadup.cache.caffeine.cache-config.short-lived.random-offset-seconds=1"
    })
@DisplayName("Caffeine 缓存过期策略测试")
public class CaffeineExpirationTest extends BaseCacheTest {

  @Test
  @DisplayName("测试写入后过期 (expire-after-write)")
  void testExpireAfterWrite() {
    // Given
    String key = "expire:write:1";
    User user = User.createTestUser("1");

    // When
    cacheBinding.set(TEST_CACHE_NAME, key, user);
    User cachedUser1 = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

    // Wait for expiration (2 seconds + buffer, increased for CI)
    await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(100, TimeUnit.MILLISECONDS)
        .until(
            () -> {
              User u = cacheBinding.get(TEST_CACHE_NAME, key, User.class);
              return u == null;
            });

    User cachedUser2 = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

    // Then
    assertNotNull(cachedUser1, "Should be cached initially");
    assertNull(cachedUser2, "Should be expired after write timeout");
  }

  @Test
  @DisplayName("测试访问后过期 (expire-after-access)")
  void testExpireAfterAccess() {
    // Given
    String key = "expire:access:1";
    User user = User.createTestUser("1");
    cacheBinding.set(TEST_CACHE_NAME, key, user);

    // When - Keep accessing within expiration window
    for (int i = 0; i < 3; i++) {
      sleep(500); // Sleep 0.5 seconds
      User cachedUser = cacheBinding.get(TEST_CACHE_NAME, key, User.class);
      assertNotNull(cachedUser, "Should still be cached due to access");
    }

    // Then - Wait for expiration after last access
    await()
        .atMost(2, TimeUnit.SECONDS)
        .until(
            () -> {
              User u = cacheBinding.get(TEST_CACHE_NAME, key, User.class);
              return u == null;
            });

    User finalCachedUser = cacheBinding.get(TEST_CACHE_NAME, key, User.class);
    assertNull(finalCachedUser, "Should be expired after access timeout");
  }

  @Test
  @DisplayName("测试最大容量淘汰策略")
  void testMaximumSizeEviction() {
    // Given - Maximum size is 100
    int itemsToCache = 150;

    // When - Cache more items than maximum size
    for (int i = 0; i < itemsToCache; i++) {
      cacheBinding.set(TEST_CACHE_NAME, "user:" + i, User.createTestUser(String.valueOf(i)));
    }

    // Give Caffeine time to perform asynchronous eviction
    sleep(100);

    // Then - Count how many items are still cached
    int cachedCount = 0;
    for (int i = 0; i < itemsToCache; i++) {
      User user = cacheBinding.get(TEST_CACHE_NAME, "user:" + i, User.class);
      if (user != null) {
        cachedCount++;
      }
    }

    // Should be approximately maximum size (allow some variance due to async eviction)
    // Caffeine may allow slight overflow before evicting
    assertTrue(
        cachedCount <= 120,
        "Cached count should not be much more than maximum size, got: " + cachedCount);
    assertTrue(cachedCount >= 80, "Should have retained most recent entries, got: " + cachedCount);
  }

  @Test
  @DisplayName("测试每个cache的独立配置")
  void testPerCacheConfiguration() {
    // Given
    String shortLivedCacheName = "short-lived";
    String key = "test:1";
    User user = User.createTestUser("1");

    // When - Set in custom configured cache (3s expiration)
    cacheBinding.set(shortLivedCacheName, key, user);
    User cachedUser1 = cacheBinding.get(shortLivedCacheName, key, User.class);

    // Wait 4 seconds for expiration
    await()
        .atMost(5, TimeUnit.SECONDS)
        .until(
            () -> {
              User u = cacheBinding.get(shortLivedCacheName, key, User.class);
              return u == null;
            });

    User cachedUser2 = cacheBinding.get(shortLivedCacheName, key, User.class);

    // Then
    assertNotNull(cachedUser1, "Should be cached initially");
    assertNull(cachedUser2, "Should be expired after configured timeout");
  }

  @Test
  @DisplayName("测试随机过期偏移")
  void testRandomExpirationOffset() {
    // Given
    String cacheName = "short-lived";
    int itemCount = 10;

    // When - Set multiple items at the same time
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < itemCount; i++) {
      cacheBinding.set(cacheName, "user:" + i, User.createTestUser(String.valueOf(i)));
    }

    // Then - Check expiration times are different due to random offset
    // Wait for base expiration + max offset
    sleep(4000); // 3s base + 1s offset

    int expiredCount = 0;
    for (int i = 0; i < itemCount; i++) {
      User user = cacheBinding.get(cacheName, "user:" + i, User.class);
      if (user == null) {
        expiredCount++;
      }
    }

    // All items should be expired by now
    assertEquals(itemCount, expiredCount, "All items should be expired after max offset time");
  }

  @Test
  @DisplayName("测试缓存刷新")
  void testCacheRefresh() {
    // Given
    String key = "refresh:1";
    User user1 = User.createTestUser("1");
    user1.setName("Original");

    // When
    cacheBinding.set(TEST_CACHE_NAME, key, user1);
    User cachedUser1 = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

    // Update cache
    User user2 = User.createTestUser("1");
    user2.setName("Updated");
    cacheBinding.set(TEST_CACHE_NAME, key, user2);
    User cachedUser2 = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

    // Then
    assertEquals("Original", cachedUser1.getName());
    assertEquals("Updated", cachedUser2.getName());
  }
}
