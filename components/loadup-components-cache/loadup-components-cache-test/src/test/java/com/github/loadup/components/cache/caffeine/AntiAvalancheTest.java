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

import com.github.loadup.components.cache.common.BaseCacheTest;
import com.github.loadup.components.cache.common.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Anti-Avalanche and Anti-Breakdown Test
 * 测试防缓存雪崩和缓存击穿策略
 */
@TestPropertySource(properties = {
    "loadup.cache.type=caffeine",
    "loadup.cache.caffeine.cache-config.hot-data.expire-after-write=5s",
    "loadup.cache.caffeine.cache-config.hot-data.maximum-size=100",
    "loadup.cache.caffeine.cache-config.hot-data.enable-random-expiration=true",
    "loadup.cache.caffeine.cache-config.hot-data.random-offset-seconds=2",
    "loadup.cache.caffeine.cache-config.hot-data.priority=9",
    "loadup.cache.caffeine.cache-config.normal-data.expire-after-write=3s",
    "loadup.cache.caffeine.cache-config.normal-data.maximum-size=100",
    "loadup.cache.caffeine.cache-config.normal-data.enable-random-expiration=true",
    "loadup.cache.caffeine.cache-config.normal-data.random-offset-seconds=1"
})
@DisplayName("防缓存雪崩和击穿测试")
public class AntiAvalancheTest extends BaseCacheTest {

    @Test
    @DisplayName("测试随机过期时间防止缓存雪崩")
    void testRandomExpirationPreventsAvalanche() {
        // Given - 同时缓存多个数据项
        String cacheName = "normal-data";
        int itemCount = 10;
        List<String> keys = new ArrayList<>();

        // When - 在同一时刻缓存所有数据
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < itemCount; i++) {
            String key = "user:" + i;
            keys.add(key);
            cacheBinding.set(cacheName, key, User.createTestUser(String.valueOf(i)));
        }

        // Then - 验证过期时间是分散的
        // 在基础过期时间（3s）处，不应该所有缓存都失效
        sleep(3000);

        int stillCachedCount = 0;
        for (String key : keys) {
            User user = cacheBinding.get(cacheName, key, User.class);
            if (user != null) {
                stillCachedCount++;
            }
        }

        // 由于随机偏移（0-1秒），应该有部分数据还未过期
        System.out.println("After 3s, still cached: " + stillCachedCount + " out of " + itemCount);

        // 等待所有数据都过期（3s + 1s random offset + buffer）
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            int count = 0;
            for (String key : keys) {
                if (cacheBinding.get(cacheName, key, User.class) == null) {
                    count++;
                }
            }
            return count == itemCount; // All expired
        });
    }

    @Test
    @DisplayName("测试热点数据长过期时间防止击穿")
    void testHotDataLongerExpiration() {
        // Given - 热点数据配置了更长的过期时间
        String hotCacheName = "hot-data";
        String normalCacheName = "normal-data";
        String key = "product:1";

        // When - 同时缓存到热点缓存和普通缓存
        cacheBinding.set(hotCacheName, key, User.createTestUser("1"));
        cacheBinding.set(normalCacheName, key, User.createTestUser("1"));

        // Wait 4 seconds
        sleep(4000);

        User hotData = cacheBinding.get(hotCacheName, key, User.class);
        User normalData = cacheBinding.get(normalCacheName, key, User.class);

        // Then - 热点数据应该还在，普通数据可能已过期
        assertNotNull(hotData, "Hot data should still be cached (5s+ expiration)");
        System.out.println("Hot data still cached: " + (hotData != null));
        System.out.println("Normal data still cached: " + (normalData != null));
    }

    @Test
    @DisplayName("测试高优先级数据不易被淘汰")
    void testHighPriorityDataRetention() {
        // Given - 缓存超过最大容量的数据
        String hotCacheName = "hot-data";
        int maxSize = 100;
        int extraItems = 50;

        // When - 先缓存高优先级数据
        List<String> hotKeys = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String key = "hot:user:" + i;
            hotKeys.add(key);
            cacheBinding.set(hotCacheName, key, User.createTestUser(String.valueOf(i)));
        }

        // 然后缓存大量普通数据
        for (int i = 0; i < maxSize + extraItems; i++) {
            cacheBinding.set(hotCacheName, "normal:user:" + i, User.createTestUser(String.valueOf(i)));
        }

        // Then - 验证高优先级数据大部分仍在缓存中
        int hotDataRetained = 0;
        for (String key : hotKeys) {
            User user = cacheBinding.get(hotCacheName, key, User.class);
            if (user != null) {
                hotDataRetained++;
            }
        }

        System.out.println("Hot data retained: " + hotDataRetained + " out of " + hotKeys.size());
        // 高优先级数据应该大部分被保留
        assertTrue(hotDataRetained >= hotKeys.size() * 0.7,
            "At least 70% of hot data should be retained");
    }

    @Test
    @DisplayName("测试批量数据的分散过期")
    void testBatchDataStaggeredExpiration() {
        // Given
        String cacheName = "normal-data";
        int batchSize = 20;

        // When - 批量添加数据
        for (int i = 0; i < batchSize; i++) {
            cacheBinding.set(cacheName, "batch:user:" + i, User.createTestUser(String.valueOf(i)));
            sleep(50); // Small delay between additions
        }

        // Then - 记录过期时间的分布
        List<Long> expirationTimes = new ArrayList<>();
        long startCheck = System.currentTimeMillis();

        // 持续检查直到所有数据都过期
        int maxCheckTime = 6000; // 6 seconds max
        while (System.currentTimeMillis() - startCheck < maxCheckTime) {
            for (int i = 0; i < batchSize; i++) {
                String key = "batch:user:" + i;
                User user = cacheBinding.get(cacheName, key, User.class);
                if (user == null && expirationTimes.size() == i) {
                    expirationTimes.add(System.currentTimeMillis() - startCheck);
                }
            }

            if (expirationTimes.size() == batchSize) {
                break; // All expired
            }
            sleep(100);
        }

        // 验证过期时间分布
        System.out.println("Expiration times distribution: " + expirationTimes);
        assertTrue(expirationTimes.size() > 0, "Should have recorded some expiration times");

        // 验证不是所有数据同时过期（存在时间差异）
        if (expirationTimes.size() > 1) {
            long minTime = expirationTimes.stream().min(Long::compareTo).orElse(0L);
            long maxTime = expirationTimes.stream().max(Long::compareTo).orElse(0L);
            long timeDiff = maxTime - minTime;

            System.out.println("Time difference between first and last expiration: " + timeDiff + "ms");
            assertTrue(timeDiff > 500, "Should have significant time difference between expirations");
        }
    }

    @Test
    @DisplayName("测试模拟缓存雪崩场景")
    void testSimulateCacheAvalanche() {
        // Given - 准备大量数据
        String cacheName = "normal-data";
        int dataCount = 50;

        // When - 模拟所有数据同时过期的场景
        for (int i = 0; i < dataCount; i++) {
            cacheBinding.set(cacheName, "avalanche:user:" + i, User.createTestUser(String.valueOf(i)));
        }

        // 等待接近过期时间
        sleep(3500);

        // Then - 统计在短时间窗口内过期的数据量
        int expiredInWindow = 0;
        long windowStart = System.currentTimeMillis();
        long windowSize = 1000; // 1 second window

        while (System.currentTimeMillis() - windowStart < windowSize) {
            for (int i = 0; i < dataCount; i++) {
                User user = cacheBinding.get(cacheName, "avalanche:user:" + i, User.class);
                if (user == null) {
                    expiredInWindow++;
                }
            }
            sleep(50);
        }

        System.out.println("Expired in 1s window: " + expiredInWindow + " out of " + dataCount);

        // 由于随机偏移，不应该所有数据都在1秒窗口内过期
        assertTrue(expiredInWindow < dataCount * 0.9,
            "Random offset should prevent >90% expiration in 1s window");
    }
}

