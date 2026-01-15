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

import com.github.loadup.components.cache.test.common.model.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.*;

/** Redis Cache Basic Operations Test */
@TestPropertySource(properties = {"loadup.cache.binder=redis", })
@DisplayName("Redis 缓存基础操作测试")
public class RedisBasicOperationsIT extends BaseRedisCacheTest {

  @Test
  @DisplayName("测试基本的 set 和 get 操作")
  @Order(1)
  void testBasicSetAndGet() {
    // Given
    String key = "user:1";
    User user = User.createTestUser("1");

    // When
    boolean setResult = redisBinding.set(key, user);
    User cachedUser = redisBinding.getObject(key, User.class);

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
  @Order(2)
  void testGetNonExistentKey() {
    // When
    User cachedUser = redisBinding.getObject("non-existent-key", User.class);

    // Then
    assertNull(cachedUser, "Non-existent key should return null");
  }

  @Test
  @DisplayName("测试 delete 操作")
  @Order(3)
  void testDelete() {
    // Given
    String key = "user:2";
    User user = User.createTestUser("2");
    redisBinding.set(key, user);

    // When
    boolean deleteResult = redisBinding.delete(key);
    User cachedUser = redisBinding.getObject(key, User.class);

    // Then
    assertTrue(deleteResult, "Delete operation should return true");
    assertNull(cachedUser, "Deleted key should return null");
  }

  @Test
  @DisplayName("测试 deleteAll 操作")
  @Order(4)
  void testDeleteAll() {
    // Given
    redisBinding.set("user:1", User.createTestUser("1"));
    redisBinding.set("user:2", User.createTestUser("2"));
    redisBinding.set("user:3", User.createTestUser("3"));

    // When
    boolean deleteAllResult = redisBinding.deleteAll(Arrays.asList("user:1", "user:2", "user:3"));
    User user1 = redisBinding.getObject("user:1", User.class);
    User user2 = redisBinding.getObject("user:2", User.class);
    User user3 = redisBinding.getObject("user:3", User.class);

    // Then
    assertTrue(deleteAllResult, "DeleteAll operation should return true");
    assertNull(user1, "All keys should be deleted");
    assertNull(user2, "All keys should be deleted");
    assertNull(user3, "All keys should be deleted");
  }

  @Test
  @DisplayName("测试覆盖已存在的key")
  @Order(5)
  void testOverwriteExistingKey() {
    // Given
    String key = "user:4";
    User user1 = User.createTestUser("4");
    user1.setName("Original Name");

    User user2 = User.createTestUser("4");
    user2.setName("Updated Name");

    // When
    redisBinding.set(key, user1);
    User cachedUser1 = redisBinding.getObject(key, User.class);

    redisBinding.set(key, user2);
    User cachedUser2 = redisBinding.getObject(key, User.class);

    // Then
    assertEquals("Original Name", cachedUser1.getName());
    assertEquals("Updated Name", cachedUser2.getName());
  }

  @Test
  @DisplayName("测试缓存复杂对象")
  @Order(6)
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
    redisBinding.set(key, product);
    Product cachedProduct = redisBinding.getObject(key, Product.class);

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
  @Order(7)
  void testCacheCollectionObject() {
    // Given
    String key = "users:list";
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      users.add(User.createTestUser(String.valueOf(i)));
    }

    // When
    redisBinding.set(key, users);
    Object cachedObject = redisBinding.get(key);

    // Then
    assertNotNull(cachedObject);
    assertTrue(cachedObject instanceof List, "Should be a List");
  }

  @Test
  @DisplayName("测试缓存 String 值")
  @Order(8)
  void testCacheStringValue() {
    // Given
    String key = "message:1";
    String value = "Hello Redis Cache!";

    // When
    redisBinding.set(key, value);
    Object cachedValue = redisBinding.get(key);

    // Then
    assertNotNull(cachedValue);
    assertEquals(value, cachedValue);
  }

  @Test
  @DisplayName("测试缓存 Integer 值")
  @Order(9)
  void testCacheIntegerValue() {
    // Given
    String key = "count:1";
    Integer value = 12345;

    // When
    redisBinding.set(key, value);
    Object cachedValue = redisBinding.get(key);

    // Then
    assertNotNull(cachedValue);
    assertEquals(value, cachedValue);
  }

  @Test
  @DisplayName("测试删除不存在的key")
  @Order(10)
  void testDeleteNonExistentKey() {
    // When
    boolean deleteResult = redisBinding.delete("non-existent-key");

    // Then
    assertTrue(deleteResult, "Delete non-existent key should return true");
  }

  @Test
  @DisplayName("测试多次删除同一个key")
  @Order(11)
  void testMultipleDeleteSameKey() {
    // Given
    String key = "user:10";
    User user = User.createTestUser("10");
    redisBinding.set(key, user);

    // When
    boolean delete1 = redisBinding.delete(key);
    boolean delete2 = redisBinding.delete(key);

    // Then
    assertTrue(delete1, "First delete should return true");
    assertTrue(delete2, "Second delete should also return true");
  }
}
