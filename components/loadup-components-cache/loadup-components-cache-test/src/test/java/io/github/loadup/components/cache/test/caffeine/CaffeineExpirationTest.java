package io.github.loadup.components.cache.test.caffeine;

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

import static org.junit.jupiter.api.Assertions.*;

import io.github.loadup.components.cache.test.common.BaseCacheTest;
import io.github.loadup.components.cache.test.common.model.User;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caffeine Cache Expiration Strategy Test */
@Slf4j
@TestPropertySource(
    properties = {
      "loadup.cache.binder=caffeine",
      "loadup.cache.binders.caffeine.expireAfterWrite=20s",
      "loadup.cache.binders.caffeine.expireAfterAccess=2s",
      "loadup.cache.binders.caffeine.maximumSize=50"
    })
@DisplayName("Caffeine 缓存过期策略测试")
public class CaffeineExpirationTest extends BaseCacheTest {
  // 假设测试配置中覆盖了 defaultCacheTicker
  @Test
  @DisplayName("测试写入后过期 (expire-after-write)")
  void testExpireAfterWrite() {
    // Given
    String key = "expire:write:1";
    User user = User.createTestUser("1");

    // When
    caffeineBinding.set(key, user);
    User cachedUser1 = caffeineBinding.getObject(key, User.class);

    // When: 模拟时间前进 23 秒（超过 20 秒过期线）
    fakeTicker.advance(23, TimeUnit.SECONDS);

    User cachedUser2 = caffeineBinding.getObject(key, User.class);
    assertNotNull(cachedUser1, "Should be cached initially");

    // Then
    assertNull(cachedUser2, "Should be expired after write timeout");
  }

  @Test
  @DisplayName("测试访问后过期 (expire-after-access)")
  void testExpireAfterAccess() {
    // Given
    String key = "expire:access:1";
    User user = User.createTestUser("1");
    caffeineBinding.set(key, user);

    // When - Keep accessing within expiration window
    for (int i = 0; i < 3; i++) {
      fakeTicker.advance(1500, TimeUnit.MILLISECONDS); // 前进 1.5
      User cachedUser = caffeineBinding.getObject(key, User.class);
      assertNotNull(cachedUser, "Access in " + i + " times, Should still be cached due to access");
    }

    // Then - 模拟最后一次访问后再过 2.1s
    fakeTicker.advance(2100, TimeUnit.MILLISECONDS);
    caffeineBinding.cleanUp();

    User finalCachedUser = caffeineBinding.getObject(key, User.class);
    assertNull(finalCachedUser, "Should be expired after access timeout");
  }

  @Test
  @DisplayName("测试最大容量淘汰策略")
  void testMaximumSizeEviction() {
    // Given - Maximum size is 50 for test-cache
    int itemsToCache = 100;

    // When - Cache more items than maximum size
    for (int i = 0; i < itemsToCache; i++) {
      caffeineBinding.set("user:" + i, User.createTestUser(String.valueOf(i)));
    }

    // Give Caffeine time to perform asynchronous eviction
    sleep(200);

    // Then - Count how many items are still cached
    int cachedCount = 0;
    for (int i = 0; i < itemsToCache; i++) {
      User user = caffeineBinding.getObject("user:" + i, User.class);
      if (user != null) {
        cachedCount++;
      }
    }

    // Should be approximately maximum size (50, allow some variance due to async eviction)
    // Caffeine may allow slight overflow before evicting
    assertTrue(
        cachedCount <= 50,
        "Cached count should not be much more than maximum size (50), got: " + cachedCount);
  }

  @Test
  @DisplayName("测试缓存刷新")
  void testCacheRefresh() {
    // Given
    String key = "refresh:1";
    User user1 = User.createTestUser("1");
    user1.setName("Original");

    // When
    caffeineBinding.set(key, user1);
    User cachedUser1 = caffeineBinding.getObject(key, User.class);

    // Update cache
    User user2 = User.createTestUser("1");
    user2.setName("Updated");
    caffeineBinding.set(key, user2);
    User cachedUser2 = caffeineBinding.getObject(key, User.class);

    // Then
    assertEquals("Original", cachedUser1.getName());
    assertEquals("Updated", cachedUser2.getName());
  }
}
