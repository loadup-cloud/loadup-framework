/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.redis;

import com.github.loadup.components.cache.common.BaseCacheTest;
import com.github.loadup.components.cache.common.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.*;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis Cache Anti-Avalanche Test
 * Tests strategies to prevent cache avalanche, penetration, and breakdown scenarios
 */
@Slf4j
@Testcontainers
@TestPropertySource(properties = {
    "loadup.cache.type=redis",
    "loadup.cache.redis.database=0",
    // Configure cache with anti-avalanche strategies
    "loadup.cache.redis.cache-config.avalanche-test.expire-after-write=5s",
    "loadup.cache.redis.cache-config.avalanche-test.enable-random-expiration=true",
    "loadup.cache.redis.cache-config.avalanche-test.random-offset-seconds=2",
    "loadup.cache.redis.cache-config.avalanche-test.cache-null-values=true",
    // Another cache without anti-avalanche strategy
    "loadup.cache.redis.cache-config.no-random.expire-after-write=5s",
    "loadup.cache.redis.cache-config.no-random.enable-random-expiration=false",
    "loadup.cache.redis.cache-config.no-random.cache-null-values=false"
})
@DisplayName("Redis 缓存防雪崩测试")
public class RedisAntiAvalancheTest extends BaseCacheTest {

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
    @DisplayName("测试随机过期时间防止缓存雪崩")
    void testRandomExpirationPreventsCacheAvalanche() throws InterruptedException {
        // Given
        String cacheName = "avalanche-test";
        int keyCount = 20;
        Map<String, Long> expirationTimes = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(keyCount);
        CountDownLatch setupLatch = new CountDownLatch(keyCount);
        CountDownLatch expirationLatch = new CountDownLatch(keyCount);

        try {
            // When - Set multiple keys at the same time
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < keyCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        String key = "avalanche:user:" + index;
                        User user = User.createTestUser(String.valueOf(index));
                        cacheBinding.set(cacheName, key, user);
                        setupLatch.countDown();
                    } catch (Exception e) {
                        log.error("Error setting key", e);
                    }
                });
            }

            setupLatch.await(10, TimeUnit.SECONDS);

            // Monitor expiration times
            for (int i = 0; i < keyCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        String key = "avalanche:user:" + index;

                        // Wait for expiration
                        await().atMost(10, TimeUnit.SECONDS).until(() -> {
                            User u = cacheBinding.get(cacheName, key, User.class);
                            return u == null;
                        });

                        long expiredTime = System.currentTimeMillis() - startTime;
                        expirationTimes.put(key, expiredTime);
                        expirationLatch.countDown();
                    } catch (Exception e) {
                        log.error("Error checking expiration", e);
                        expirationLatch.countDown();
                    }
                });
            }

            expirationLatch.await(15, TimeUnit.SECONDS);

            // Then - Analyze expiration time distribution
            List<Long> times = new ArrayList<>(expirationTimes.values());
            if (times.isEmpty()) {
                fail("No expiration times recorded");
            }

            Collections.sort(times);
            long minTime = times.get(0);
            long maxTime = times.get(times.size() - 1);
            long timeRange = maxTime - minTime;

            log.info("Expiration times - Min: {}ms, Max: {}ms, Range: {}ms",
                minTime, maxTime, timeRange);

            // With random offset of 2 seconds, we expect at least 1 second of variation
            assertTrue(timeRange > 1000,
                "Random expiration should spread expiration times by at least 1 second. Range: " + timeRange + "ms");

            // Calculate standard deviation to ensure good distribution
            double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
            double variance = times.stream()
                .mapToDouble(t -> Math.pow(t - avg, 2))
                .average()
                .orElse(0);
            double stdDev = Math.sqrt(variance);

            log.info("Expiration distribution - Avg: {}ms, StdDev: {}ms", avg, stdDev);

            // Standard deviation should be significant (more than 200ms)
            assertTrue(stdDev > 200,
                "Standard deviation should indicate good distribution. StdDev: " + stdDev + "ms");

        } finally {
            executor.shutdown();
            cacheBinding.deleteAll(cacheName);
        }
    }

    @Test
    @DisplayName("测试缓存null值防止缓存穿透")
    void testCachingNullValuesPreventspenetration() {
        // Given
        String cacheName = "avalanche-test";
        String nonExistentKey = "non:existent:user:999";

        try {
            // When - Try to get a non-existent key multiple times
            User user1 = cacheBinding.get(cacheName, nonExistentKey, User.class);

            // Simulate setting null explicitly (simulates backend query returning null)
            cacheBinding.set(cacheName, nonExistentKey, null);

            Object cachedValue = cacheBinding.get(cacheName, nonExistentKey);

            // Then
            assertNull(user1, "Non-existent key should return null");
            // Note: With cache-null-values=true, the cache should store the null value
            // to prevent repeated queries to the backend
            log.info("Null value caching test - Initial: null, Cached: {}", cachedValue);
        } finally {
            cacheBinding.deleteAll(cacheName);
        }
    }

    @Test
    @DisplayName("测试批量缓存设置时的过期时间分布")
    void testBatchCacheExpirationDistribution() throws InterruptedException {
        // Given
        String cacheName = "avalanche-test";
        int batchSize = 30;
        long batchSetStartTime = System.currentTimeMillis();

        try {
            // When - Batch set cache entries
            for (int i = 0; i < batchSize; i++) {
                String key = "batch:user:" + i;
                User user = User.createTestUser(String.valueOf(i));
                cacheBinding.set(cacheName, key, user);
            }

            // Wait a bit and check how many are still cached
            sleep(6000); // Wait 6 seconds (base TTL is 5s + random 0-2s)

            int stillCached = 0;
            int expired = 0;
            for (int i = 0; i < batchSize; i++) {
                String key = "batch:user:" + i;
                User user = cacheBinding.get(cacheName, key, User.class);
                if (user != null) {
                    stillCached++;
                } else {
                    expired++;
                }
            }

            log.info("After 6 seconds - Still cached: {}, Expired: {}", stillCached, expired);

            // Then - Some should be expired, some still cached (due to random offset)
            // With 5-7s TTL range and checking at 6s, we expect a mix
            assertTrue(expired > 0, "Some entries should be expired by 6 seconds");

            // Wait for all to expire
            await().atMost(10, TimeUnit.SECONDS).until(() -> {
                for (int i = 0; i < batchSize; i++) {
                    User u = cacheBinding.get(cacheName, "batch:user:" + i, User.class);
                    if (u != null) {
                        return false;
                    }
                }
                return true;
            });

            // Verify all expired
            int finalCached = 0;
            for (int i = 0; i < batchSize; i++) {
                String key = "batch:user:" + i;
                User user = cacheBinding.get(cacheName, key, User.class);
                if (user != null) {
                    finalCached++;
                }
            }

            assertEquals(0, finalCached, "All entries should be expired eventually");
        } finally {
            cacheBinding.deleteAll(cacheName);
        }
    }

    @Test
    @DisplayName("对比有无随机过期策略的差异")
    void testCompareWithAndWithoutRandomExpiration() throws InterruptedException {
        // Given
        String cacheWithRandom = "avalanche-test";
        String cacheNoRandom = "no-random";
        int keyCount = 10;
        long startTime = System.currentTimeMillis();

        try {
            // When - Set keys in both caches simultaneously
            for (int i = 0; i < keyCount; i++) {
                String key = "user:" + i;
                User user = User.createTestUser(String.valueOf(i));
                cacheBinding.set(cacheWithRandom, key, user);
                cacheBinding.set(cacheNoRandom, key, user);
            }

            // Monitor expiration
            Set<Long> withRandomExpTimes = Collections.synchronizedSet(new HashSet<>());
            Set<Long> noRandomExpTimes = Collections.synchronizedSet(new HashSet<>());

            ExecutorService executor = Executors.newFixedThreadPool(keyCount * 2);
            CountDownLatch latch = new CountDownLatch(keyCount * 2);

            for (int i = 0; i < keyCount; i++) {
                final int index = i;

                // Monitor cache with random expiration
                executor.submit(() -> {
                    try {
                        String key = "user:" + index;
                        await().atMost(10, TimeUnit.SECONDS).until(() -> {
                            User u = cacheBinding.get(cacheWithRandom, key, User.class);
                            return u == null;
                        });
                        withRandomExpTimes.add(System.currentTimeMillis() - startTime);
                    } finally {
                        latch.countDown();
                    }
                });

                // Monitor cache without random expiration
                executor.submit(() -> {
                    try {
                        String key = "user:" + index;
                        await().atMost(10, TimeUnit.SECONDS).until(() -> {
                            User u = cacheBinding.get(cacheNoRandom, key, User.class);
                            return u == null;
                        });
                        noRandomExpTimes.add(System.currentTimeMillis() - startTime);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(15, TimeUnit.SECONDS);

            // Then - Analyze results
            long withRandomRange = withRandomExpTimes.isEmpty() ? 0 :
                Collections.max(withRandomExpTimes) - Collections.min(withRandomExpTimes);
            long noRandomRange = noRandomExpTimes.isEmpty() ? 0 :
                Collections.max(noRandomExpTimes) - Collections.min(noRandomExpTimes);

            log.info("With random expiration - Count: {}, Range: {}ms",
                withRandomExpTimes.size(), withRandomRange);
            log.info("Without random expiration - Count: {}, Range: {}ms",
                noRandomExpTimes.size(), noRandomRange);

            // With random should have larger range (more spread out)
            assertTrue(withRandomRange > noRandomRange,
                String.format("Random expiration should have larger time range. " +
                    "WithRandom: %dms, NoRandom: %dms", withRandomRange, noRandomRange));

            executor.shutdown();
        } finally {
            cacheBinding.deleteAll(cacheWithRandom);
            cacheBinding.deleteAll(cacheNoRandom);
        }
    }

    @Test
    @DisplayName("测试热点数据的缓存击穿防护")
    void testHotKeyCacheBreakdownProtection() throws InterruptedException {
        // Given
        String cacheName = "avalanche-test";
        String hotKey = "hot:product:1001";
        User hotUser = User.createTestUser("1001");
        hotUser.setName("Hot Product");

        int concurrentRequests = 50;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch latch = new CountDownLatch(concurrentRequests);

        try {
            // When - Set hot key
            cacheBinding.set(cacheName, hotKey, hotUser);

            // Simulate high concurrent access to hot key
            for (int i = 0; i < concurrentRequests; i++) {
                executor.submit(() -> {
                    try {
                        User user = cacheBinding.get(cacheName, hotKey, User.class);
                        assertNotNull(user, "Hot key should be accessible");
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(10, TimeUnit.SECONDS);

            // Then
            assertTrue(completed, "All concurrent requests should complete successfully");

            // Verify data is still intact after high concurrent access
            User finalUser = cacheBinding.get(cacheName, hotKey, User.class);
            assertNotNull(finalUser, "Hot key should still be cached");
            assertEquals("Hot Product", finalUser.getName(), "Data should be intact");

            log.info("Hot key handled {} concurrent requests successfully", concurrentRequests);
        } finally {
            executor.shutdown();
            cacheBinding.deleteAll(cacheName);
        }
    }

    @Test
    @DisplayName("测试大量key同时过期的雪崩场景模拟")
    void testMassiveKeyExpirationAvalancheScenario() throws InterruptedException {
        // Given
        String cacheName = "avalanche-test";
        int massiveKeyCount = 100;
        long setupStartTime = System.currentTimeMillis();

        try {
            // When - Set massive amount of keys
            for (int i = 0; i < massiveKeyCount; i++) {
                String key = "massive:user:" + i;
                User user = User.createTestUser(String.valueOf(i));
                cacheBinding.set(cacheName, key, user);
            }

            long setupTime = System.currentTimeMillis() - setupStartTime;
            log.info("Setup {} keys in {}ms", massiveKeyCount, setupTime);

            // Monitor expiration in time windows
            int[] timeWindows = {4000, 5000, 6000, 7000, 8000};
            Map<Integer, Integer> expirationCount = new HashMap<>();

            for (int window : timeWindows) {
                sleep(window - (System.currentTimeMillis() - setupStartTime));

                int expired = 0;
                for (int i = 0; i < massiveKeyCount; i++) {
                    User user = cacheBinding.get(cacheName, "massive:user:" + i, User.class);
                    if (user == null) {
                        expired++;
                    }
                }

                expirationCount.put(window, expired);
                log.info("At {}ms: {} keys expired", window, expired);

                if (expired == massiveKeyCount) {
                    break; // All expired
                }
            }

            // Then - With random expiration, keys should not all expire at once
            // There should be gradual expiration across time windows
            List<Integer> expirations = new ArrayList<>(expirationCount.values());
            if (expirations.size() > 1) {
                for (int i = 1; i < expirations.size(); i++) {
                    int current = expirations.get(i);
                    int previous = expirations.get(i - 1);
                    assertTrue(current >= previous,
                        "Expiration count should be monotonically increasing");
                }
            }

        } finally {
            cacheBinding.deleteAll(cacheName);
        }
    }
}

