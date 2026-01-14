package com.github.loadup.components.cache.test.redis;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.SecurityProperties;import org.springframework.test.context.*;

import com.github.loadup.components.cache.test.common.BaseCacheTest;
import com.github.loadup.components.cache.test.common.model.*;
/** Redis Cache Basic Operations Test */
@TestPropertySource(properties = {"loadup.cache.binder=redis", "loadup.cache.database=0"})
@DisplayName("Redis 缓存基础操作测试")
public class RedisBasicOperationsIT extends BaseCacheTest {

  @Test
  @DisplayName("测试基本的 set 和 get 操作")
  void testBasicSetAndGet() {
    // Given
    String key = "user:1";
    User user = User.createTestUser("1");

    // When
    boolean setResult = caffeineBinding.set(key, user);
    User cachedUser = caffeineBinding.get(key, User.class);

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
    User cachedUser = caffeineBinding.get("non-existent-key", User.class);

    // Then
    assertNull(cachedUser, "Non-existent key should return null");
  }

  @Test
  @DisplayName("测试 delete 操作")
  void testDelete() {
    // Given
    String key = "user:2";
    User user = User.createTestUser("2");
    caffeineBinding.set(key, user);

    // When
    boolean deleteResult = caffeineBinding.delete(key);
    User cachedUser = caffeineBinding.get(key, User.class);

    // Then
    assertTrue(deleteResult, "Delete operation should return true");
    assertNull(cachedUser, "Deleted key should return null");
  }

  @Test
  @DisplayName("测试 deleteAll 操作")
  void testDeleteAll() {
    // Given
    caffeineBinding.set("user:1", User.createTestUser("1"));
    caffeineBinding.set("user:2", User.createTestUser("2"));
    caffeineBinding.set("user:3", User.createTestUser("3"));

    // When
    boolean deleteAllResult = caffeineBinding.deleteAll(Arrays.asList("user:1", "user:2", "user:3"));
    User user1 = caffeineBinding.get("user:1", User.class);
    User user2 = caffeineBinding.get("user:2", User.class);
    User user3 = caffeineBinding.get("user:3", User.class);

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
    caffeineBinding.set(key, user1);
    User cachedUser1 = caffeineBinding.get(key, User.class);

    caffeineBinding.set(key, user2);
    User cachedUser2 = caffeineBinding.get(key, User.class);

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
    caffeineBinding.set(key, product);
    Product cachedProduct = caffeineBinding.get(key, Product.class);

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
    caffeineBinding.set(key, users);
    Object cachedObject = caffeineBinding.get(key);

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
    caffeineBinding.set(key, value);
    Object cachedValue = caffeineBinding.get(key);

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
    caffeineBinding.set(key, value);
    Object cachedValue = caffeineBinding.get(key);

    // Then
    assertNotNull(cachedValue);
    assertEquals(value, cachedValue);
  }

  @Test
  @DisplayName("测试删除不存在的key")
  void testDeleteNonExistentKey() {
    // When
    boolean deleteResult = caffeineBinding.delete("non-existent-key");

    // Then
    assertTrue(deleteResult, "Delete non-existent key should return true");
  }

  @Test
  @DisplayName("测试多次删除同一个key")
  void testMultipleDeleteSameKey() {
    // Given
    String key = "user:10";
    User user = User.createTestUser("10");
    caffeineBinding.set(key, user);

    // When
    boolean delete1 = caffeineBinding.delete(key);
    boolean delete2 = caffeineBinding.delete(key);

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
      caffeineBinding.set(key, user1);
      caffeineBinding.set(key, user2);

      User cachedUser1 = caffeineBinding.get(key, User.class);
      User cachedUser2 = caffeineBinding.get(key, User.class);

      // Then
      assertNotNull(cachedUser1);
      assertNotNull(cachedUser2);
      assertEquals("Cache 1 User", cachedUser1.getName());
      assertEquals("Cache 2 User", cachedUser2.getName());
    } finally {
      // Cleanup
//      caffeineBinding.deleteAll(cacheName1);
//      caffeineBinding.deleteAll(cacheName2);
    }
  }
}
