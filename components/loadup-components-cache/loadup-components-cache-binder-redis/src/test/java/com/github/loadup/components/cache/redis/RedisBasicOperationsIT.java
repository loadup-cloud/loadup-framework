package com.github.loadup.components.cache.redis;

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

import com.github.loadup.components.cache.common.model.Product;
import com.github.loadup.components.cache.common.model.User;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.*;

/** Redis Cache Basic Operations Test */
@TestPropertySource(properties = {"loadup.cache.type=redis", "loadup.cache.redis.database=0"})
@DisplayName("Redis 缓存基础操作测试")
public class RedisBasicOperationsIT extends BaseRedisCacheTest {

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
    assertEquals(user.getAge(), cachedUser.getAge());
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
    cacheBinding.set(TEST_CACHE_NAME, key, user1);
    User cachedUser1 = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

    cacheBinding.set(TEST_CACHE_NAME, key, user2);
    User cachedUser2 = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

    // Then
    assertEquals("Original Name", cachedUser1.getName());
    assertEquals("Updated Name", cachedUser2.getName());
  }

  @Test
  @DisplayName("测试缓存复杂对象")
  void testCacheComplexObject() {
    // Given
    String key = "product:1";
    Product product =
        Product.builder()
            .id("1")
            .name("Test Product")
            .description("Test Description")
            .price(new BigDecimal("99.99"))
            .stock(100)
            .category("Electronics")
            .build();

    // When
    cacheBinding.set(TEST_CACHE_NAME, key, product);
    Product cachedProduct = cacheBinding.get(TEST_CACHE_NAME, key, Product.class);

    // Then
    assertNotNull(cachedProduct);
    assertEquals(product.getId(), cachedProduct.getId());
    assertEquals(product.getName(), cachedProduct.getName());
    assertEquals(product.getDescription(), cachedProduct.getDescription());
    assertEquals(0, product.getPrice().compareTo(cachedProduct.getPrice()));
    assertEquals(product.getStock(), cachedProduct.getStock());
    assertEquals(product.getCategory(), cachedProduct.getCategory());
  }

  @Test
  @DisplayName("测试缓存集合对象")
  void testCacheCollectionObject() {
    // Given
    String key = "users:list";
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      users.add(User.createTestUser(String.valueOf(i)));
    }

    // When
    cacheBinding.set(TEST_CACHE_NAME, key, users);
    Object cachedObject = cacheBinding.get(TEST_CACHE_NAME, key);

    // Then
    assertNotNull(cachedObject);
    assertTrue(cachedObject instanceof List, "Should be a List");
  }

  @Test
  @DisplayName("测试缓存 String 值")
  void testCacheStringValue() {
    // Given
    String key = "message:1";
    String value = "Hello Redis Cache!";

    // When
    cacheBinding.set(TEST_CACHE_NAME, key, value);
    Object cachedValue = cacheBinding.get(TEST_CACHE_NAME, key);

    // Then
    assertNotNull(cachedValue);
    assertEquals(value, cachedValue);
  }

  @Test
  @DisplayName("测试缓存 Integer 值")
  void testCacheIntegerValue() {
    // Given
    String key = "count:1";
    Integer value = 12345;

    // When
    cacheBinding.set(TEST_CACHE_NAME, key, value);
    Object cachedValue = cacheBinding.get(TEST_CACHE_NAME, key);

    // Then
    assertNotNull(cachedValue);
    assertEquals(value, cachedValue);
  }

  @Test
  @DisplayName("测试删除不存在的key")
  void testDeleteNonExistentKey() {
    // When
    boolean deleteResult = cacheBinding.delete(TEST_CACHE_NAME, "non-existent-key");

    // Then
    assertTrue(deleteResult, "Delete non-existent key should return true");
  }

  @Test
  @DisplayName("测试多次删除同一个key")
  void testMultipleDeleteSameKey() {
    // Given
    String key = "user:10";
    User user = User.createTestUser("10");
    cacheBinding.set(TEST_CACHE_NAME, key, user);

    // When
    boolean delete1 = cacheBinding.delete(TEST_CACHE_NAME, key);
    boolean delete2 = cacheBinding.delete(TEST_CACHE_NAME, key);

    // Then
    assertTrue(delete1, "First delete should return true");
    assertTrue(delete2, "Second delete should also return true");
  }

  @Test
  @DisplayName("测试不同的 cacheName")
  void testDifferentCacheNames() {
    // Given
    String cacheName1 = "cache-1";
    String cacheName2 = "cache-2";
    String key = "user:1";
    User user1 = User.createTestUser("1");
    user1.setName("Cache 1 User");
    User user2 = User.createTestUser("1");
    user2.setName("Cache 2 User");

    try {
      // When
      cacheBinding.set(cacheName1, key, user1);
      cacheBinding.set(cacheName2, key, user2);

      User cachedUser1 = cacheBinding.get(cacheName1, key, User.class);
      User cachedUser2 = cacheBinding.get(cacheName2, key, User.class);

      // Then
      assertNotNull(cachedUser1);
      assertNotNull(cachedUser2);
      assertEquals("Cache 1 User", cachedUser1.getName());
      assertEquals("Cache 2 User", cachedUser2.getName());
    } finally {
      // Cleanup
      cacheBinding.deleteAll(cacheName1);
      cacheBinding.deleteAll(cacheName2);
    }
  }
}
