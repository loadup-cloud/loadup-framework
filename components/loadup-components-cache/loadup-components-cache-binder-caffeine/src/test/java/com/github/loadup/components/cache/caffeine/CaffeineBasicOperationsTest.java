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

import static org.junit.jupiter.api.Assertions.*;

import com.github.loadup.components.cache.common.BaseCacheTest;
import com.github.loadup.components.cache.common.model.Product;
import com.github.loadup.components.cache.common.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caffeine Cache Basic Operations Test */
@TestPropertySource(
    properties = {
      "loadup.cache.type=caffeine",
      "spring.cache.caffeine.spec=initialCapacity=100,maximumSize=1000,expireAfterWrite=300s"
    })
@DisplayName("Caffeine 缓存基础操作测试")
public class CaffeineBasicOperationsTest extends BaseCacheTest {

  @Test
  @DisplayName("测试基本的 set 和 get 操作")
  void testBasicSetAndGet() {
    // Given
    String key = "user:1";
    User user = User.createTestUser("1");

    // When
    boolean setResult = cacheBinding.set(TEST_CACHE_NAME, key, user);
    User cachedUser = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

    // Then
    assertTrue(setResult, "Set operation should return true");
    assertNotNull(cachedUser, "Cached user should not be null");
    assertEquals(user.getId(), cachedUser.getId());
    assertEquals(user.getName(), cachedUser.getName());
    assertEquals(user.getEmail(), cachedUser.getEmail());
  }

  @Test
  @DisplayName("测试缓存不存在的key")
  void testGetNonExistentKey() {
    // When
    User cachedUser = cacheBinding.get(TEST_CACHE_NAME, "non-existent-key", User.class);

    // Then
    assertNull(cachedUser, "Non-existent key should return null");
  }

  @Test
  @DisplayName("测试 delete 操作")
  void testDelete() {
    // Given
    String key = "user:2";
    User user = User.createTestUser("2");
    cacheBinding.set(TEST_CACHE_NAME, key, user);

    // When
    boolean deleteResult = cacheBinding.delete(TEST_CACHE_NAME, key);
    User cachedUser = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

    // Then
    assertTrue(deleteResult, "Delete operation should return true");
    assertNull(cachedUser, "Deleted key should return null");
  }

  @Test
  @DisplayName("测试 deleteAll 操作")
  void testDeleteAll() {
    // Given
    cacheBinding.set(TEST_CACHE_NAME, "user:1", User.createTestUser("1"));
    cacheBinding.set(TEST_CACHE_NAME, "user:2", User.createTestUser("2"));
    cacheBinding.set(TEST_CACHE_NAME, "user:3", User.createTestUser("3"));

    // When
    boolean deleteAllResult = cacheBinding.deleteAll(TEST_CACHE_NAME);
    User user1 = cacheBinding.get(TEST_CACHE_NAME, "user:1", User.class);
    User user2 = cacheBinding.get(TEST_CACHE_NAME, "user:2", User.class);
    User user3 = cacheBinding.get(TEST_CACHE_NAME, "user:3", User.class);

    // Then
    assertTrue(deleteAllResult, "DeleteAll operation should return true");
    assertNull(user1, "All keys should be deleted");
    assertNull(user2, "All keys should be deleted");
    assertNull(user3, "All keys should be deleted");
  }

  @Test
  @DisplayName("测试覆盖已存在的key")
  void testOverwriteExistingKey() {
    // Given
    String key = "user:4";
    User user1 = User.createTestUser("4");
    user1.setName("Original Name");

    User user2 = User.createTestUser("4");
    user2.setName("Updated Name");

    // When
    boolean setResult1 = cacheBinding.set(TEST_CACHE_NAME, key, user1);
    assertTrue(setResult1, "First set should succeed");
    User cachedUser1 = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

    boolean setResult2 = cacheBinding.set(TEST_CACHE_NAME, key, user2);
    assertTrue(setResult2, "Second set (overwrite) should succeed");
    User cachedUser2 = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

    // Then
    assertNotNull(cachedUser1, "First cached user should not be null");
    assertNotNull(cachedUser2, "Second cached user should not be null");
    assertEquals("Original Name", cachedUser1.getName(), "First get should return original value");
    assertEquals(
        "Updated Name", cachedUser2.getName(), "After overwrite, should return updated value");
    assertEquals("4", cachedUser2.getId(), "ID should remain the same");
  }

  @Test
  @DisplayName("测试不同类型的对象")
  void testDifferentObjectTypes() {
    // Given
    String userKey = "user:5";
    String productKey = "product:1";
    User user = User.createTestUser("5");
    Product product = Product.createTestProduct("1");

    // When
    cacheBinding.set(TEST_CACHE_NAME, userKey, user);
    cacheBinding.set(TEST_CACHE_NAME, productKey, product);

    User cachedUser = cacheBinding.get(TEST_CACHE_NAME, userKey, User.class);
    Product cachedProduct = cacheBinding.get(TEST_CACHE_NAME, productKey, Product.class);

    // Then
    assertNotNull(cachedUser);
    assertNotNull(cachedProduct);
    assertEquals(user.getId(), cachedUser.getId());
    assertEquals(product.getId(), cachedProduct.getId());
  }

  @Test
  @DisplayName("测试缓存String类型")
  void testCacheStringValue() {
    // Given
    String key = "string:key";
    String value = "Hello, Cache!";

    // When
    cacheBinding.set(TEST_CACHE_NAME, key, value);
    Object cachedValue = cacheBinding.get(TEST_CACHE_NAME, key);

    // Then
    assertNotNull(cachedValue);
    assertEquals(value, cachedValue.toString());
  }

  @Test
  @DisplayName("测试批量操作")
  void testBatchOperations() {
    // Given
    int batchSize = 100;

    // When - 批量写入
    for (int i = 0; i < batchSize; i++) {
      cacheBinding.set(TEST_CACHE_NAME, "user:" + i, User.createTestUser(String.valueOf(i)));
    }

    // Then - 验证所有数据都被缓存
    for (int i = 0; i < batchSize; i++) {
      User user = cacheBinding.get(TEST_CACHE_NAME, "user:" + i, User.class);
      assertNotNull(user, "User " + i + " should be cached");
      assertEquals(String.valueOf(i), user.getId());
    }
  }

  @Test
  @DisplayName("测试空值处理")
  void testNullValue() {
    // Given
    String key = "null:key";

    // When & Then - Caffeine 默认不允许空值
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> cacheBinding.set(TEST_CACHE_NAME, key, null));
    assertTrue(exception.getMessage().contains("null"));
  }
}
