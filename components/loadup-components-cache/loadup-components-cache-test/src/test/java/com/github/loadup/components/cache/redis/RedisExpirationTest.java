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

import com.github.loadup.components.cache.common.BaseCacheTest;
import com.github.loadup.components.cache.common.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis Cache Expiration Strategy Test
 */
@Testcontainers
@TestPropertySource(properties = {
    "loadup.cache.type=redis",
    "loadup.cache.redis.database=0",
    // Configure specific cache with short TTL for testing
    "loadup.cache.redis.cache-config.short-lived.expire-after-write=3s",
    "loadup.cache.redis.cache-config.short-lived.cache-null-values=true",
    "loadup.cache.redis.cache-config.short-lived.enable-random-expiration=true",
    "loadup.cache.redis.cache-config.short-lived.random-offset-seconds=1",
    // Configure another cache with different TTL
    "loadup.cache.redis.cache-config.medium-lived.expire-after-write=10s",
    "loadup.cache.redis.cache-config.medium-lived.cache-null-values=false"
})
@DisplayName("Redis 缓存过期策略测试")
public class RedisExpirationTest extends BaseCacheTest {

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379)
        .withReuse(true);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("loadup.cache.redis.host", redis::getHost);
        registry.add("loadup.cache.redis.port", redis::getFirstMappedPort);
    }

    @Test
    @DisplayName("测试 TTL 过期策略")
    void testTimeToLiveExpiration() {
        // Given
        String cacheName = "short-lived";
        String key = "expire:ttl:1";
        User user = User.createTestUser("1");

        try {
            // When
            cacheBinding.set(cacheName, key, user);
            User cachedUser1 = cacheBinding.get(cacheName, key, User.class);

            // Wait for expiration (3 seconds base + 1 second random offset max = 4 seconds + buffer)
            await().atMost(6, TimeUnit.SECONDS).pollDelay(4, TimeUnit.SECONDS).until(() -> {
                User u = cacheBinding.get(cacheName, key, User.class);
                return u == null;
            });

            User cachedUser2 = cacheBinding.get(cacheName, key, User.class);

            // Then
            assertNotNull(cachedUser1, "Should be cached initially");
            assertNull(cachedUser2, "Should be expired after TTL");
        } finally {
            cacheBinding.deleteAll(cacheName);
        }
    }

    @Test
    @DisplayName("测试不同 cacheName 的不同过期时间")
    void testDifferentExpirationForDifferentCaches() {
        // Given
        String shortLivedCache = "short-lived";
        String mediumLivedCache = "medium-lived";
        String key = "user:1";
        User user = User.createTestUser("1");

        try {
            // When
            cacheBinding.set(shortLivedCache, key, user);
            cacheBinding.set(mediumLivedCache, key, user);

            // Wait for short-lived cache to expire (max 4 seconds)
            await().atMost(6, TimeUnit.SECONDS).pollDelay(4, TimeUnit.SECONDS).until(() -> {
                User u = cacheBinding.get(shortLivedCache, key, User.class);
                return u == null;
            });

            User shortLivedUser = cacheBinding.get(shortLivedCache, key, User.class);
            User mediumLivedUser = cacheBinding.get(mediumLivedCache, key, User.class);

            // Then
            assertNull(shortLivedUser, "Short-lived cache should be expired");
            assertNotNull(mediumLivedUser, "Medium-lived cache should still be valid");
        } finally {
            cacheBinding.deleteAll(shortLivedCache);
            cacheBinding.deleteAll(mediumLivedCache);
        }
    }

    @Test
    @DisplayName("测试随机过期偏移防止缓存雪崩")
    void testRandomExpirationOffset() {
        // Given
        String cacheName = "short-lived";
        int testCount = 10;
        long[] expirationTimes = new long[testCount];

        try {
            // When - Set multiple keys and record their expiration times
            for (int i = 0; i < testCount; i++) {
                String key = "user:" + i;
                User user = User.createTestUser(String.valueOf(i));

                long startTime = System.currentTimeMillis();
                cacheBinding.set(cacheName, key, user);

                // Wait for expiration
                await().atMost(6, TimeUnit.SECONDS).until(() -> {
                    User u = cacheBinding.get(cacheName, key, User.class);
                    return u == null;
                });

                expirationTimes[i] = System.currentTimeMillis() - startTime;
            }

            // Then - Verify that expiration times are not all the same (within a small margin)
            long minExpiration = Long.MAX_VALUE;
            long maxExpiration = Long.MIN_VALUE;

            for (long time : expirationTimes) {
                minExpiration = Math.min(minExpiration, time);
                maxExpiration = Math.max(maxExpiration, time);
            }

            long difference = maxExpiration - minExpiration;

            // The difference should be at least a few hundred milliseconds due to random offset
            assertTrue(difference > 500,
                "Expiration times should vary due to random offset. Min: " + minExpiration +
                    "ms, Max: " + maxExpiration + "ms, Diff: " + difference + "ms");
        } finally {
            cacheBinding.deleteAll(cacheName);
        }
    }

    @Test
    @DisplayName("测试更新后重置过期时间")
    void testExpirationResetAfterUpdate() {
        // Given
        String cacheName = "short-lived";
        String key = "expire:reset:1";
        User user1 = User.createTestUser("1");
        user1.setName("Original");

        try {
            // When
            cacheBinding.set(cacheName, key, user1);

            // Wait 2 seconds (less than expiration time)
            sleep(2000);

            // Update the value
            User user2 = User.createTestUser("1");
            user2.setName("Updated");
            cacheBinding.set(cacheName, key, user2);

            // Wait another 2 seconds
            sleep(2000);

            // Should still be cached since we updated it 2 seconds ago
            User cachedUser = cacheBinding.get(cacheName, key, User.class);

            // Then
            assertNotNull(cachedUser, "Should still be cached after update");
            assertEquals("Updated", cachedUser.getName(), "Should have updated value");
        } finally {
            cacheBinding.deleteAll(cacheName);
        }
    }

    @Test
    @DisplayName("测试批量数据过期一致性")
    void testBatchExpirationConsistency() {
        // Given
        String cacheName = "short-lived";
        int batchSize = 5;

        try {
            // When - Set multiple keys at approximately the same time
            for (int i = 0; i < batchSize; i++) {
                String key = "batch:user:" + i;
                User user = User.createTestUser(String.valueOf(i));
                cacheBinding.set(cacheName, key, user);
            }

            // Verify all are cached
            for (int i = 0; i < batchSize; i++) {
                String key = "batch:user:" + i;
                User user = cacheBinding.get(cacheName, key, User.class);
                assertNotNull(user, "Key " + key + " should be cached");
            }

            // Wait for expiration
            await().atMost(6, TimeUnit.SECONDS).pollDelay(4, TimeUnit.SECONDS).until(() -> {
                for (int i = 0; i < batchSize; i++) {
                    User u = cacheBinding.get(cacheName, "batch:user:" + i, User.class);
                    if (u != null) {
                        return false; // Still have cached items
                    }
                }
                return true; // All expired
            });

            // Then - Verify all are expired
            for (int i = 0; i < batchSize; i++) {
                String key = "batch:user:" + i;
                User user = cacheBinding.get(cacheName, key, User.class);
                assertNull(user, "Key " + key + " should be expired");
            }
        } finally {
            cacheBinding.deleteAll(cacheName);
        }
    }

    @Test
    @DisplayName("测试过期后重新设置")
    void testSetAfterExpiration() {
        // Given
        String cacheName = "short-lived";
        String key = "expire:reset:2";
        User user1 = User.createTestUser("1");
        user1.setName("First");

        try {
            // When
            cacheBinding.set(cacheName, key, user1);

            // Wait for expiration
            await().atMost(6, TimeUnit.SECONDS).pollDelay(4, TimeUnit.SECONDS).until(() -> {
                User u = cacheBinding.get(cacheName, key, User.class);
                return u == null;
            });

            // Set a new value after expiration
            User user2 = User.createTestUser("2");
            user2.setName("Second");
            cacheBinding.set(cacheName, key, user2);

            User cachedUser = cacheBinding.get(cacheName, key, User.class);

            // Then
            assertNotNull(cachedUser, "Should be cached again after re-setting");
            assertEquals("Second", cachedUser.getName(), "Should have new value");
        } finally {
            cacheBinding.deleteAll(cacheName);
        }
    }
}

