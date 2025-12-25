/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.caffeine;

import com.github.loadup.components.cache.common.BaseCacheTest;
import com.github.loadup.components.cache.common.model.Product;
import com.github.loadup.components.cache.common.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Caffeine Cache Basic Operations Test
 */
@TestPropertySource(properties = {
    "loadup.cache.type=caffeine",
    "loadup.cache.caffeine.initial-capacity=100",
    "loadup.cache.caffeine.maximum-size=1000",
    "loadup.cache.caffeine.expire-after-write-seconds=300"
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
        cacheBinding.set(TEST_CACHE_NAME, key, user1);
        User cachedUser1 = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

        cacheBinding.set(TEST_CACHE_NAME, key, user2);
        User cachedUser2 = cacheBinding.get(TEST_CACHE_NAME, key, User.class);

        // Then
        assertEquals("Original Name", cachedUser1.getName());
        assertEquals("Updated Name", cachedUser2.getName());
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
        assertThrows(Exception.class, () -> cacheBinding.set(TEST_CACHE_NAME, key, null));
    }
}

