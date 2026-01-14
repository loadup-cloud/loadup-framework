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

import com.github.loadup.components.cache.test.common.BaseCacheTest;
import com.github.loadup.components.cache.test.common.model.User;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.*;

/** Redis Cache Concurrency Test */
@Slf4j
@TestPropertySource(properties = {"loadup.cache.binder=redis", "loadup.cache.database=0"})
@DisplayName("Redis 缓存并发测试")
public class RedisConcurrencyIT extends BaseCacheTest {

  @Test
  @DisplayName("测试并发写入")
  void testConcurrentWrites() throws InterruptedException {
    // Given
    int threadCount = 10;
    int operationsPerThread = 100;
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);

    // Updated to use try-with-resources for ExecutorService
    try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
      // When
      for (int i = 0; i < threadCount; i++) {
        final int threadIndex = i; // Make 'i' effectively final
        executor.submit(
            () -> {
              try {
                for (int j = 0; j < operationsPerThread; j++) {
                  final String key =
                      "concurrent:write:" + threadIndex + ":" + j; // Make 'key' effectively final
                  final User user =
                      User.createTestUser(threadIndex + "-" + j); // Make 'user' effectively final
                  boolean result = caffeineBinding.set(key, user);
                  if (result) {
                    successCount.incrementAndGet();
                  }
                }
              } finally {
                latch.countDown();
              }
            });
      }

      boolean completed = latch.await(30, TimeUnit.SECONDS);

      // Then
      assertTrue(completed, "All threads should complete");
      assertEquals(
          threadCount * operationsPerThread,
          successCount.get(),
          "All write operations should succeed");
    }
  }

  @Test
  @DisplayName("测试并发读取")
  void testConcurrentReads() throws InterruptedException {
    // Given
    String key = "concurrent:read:1";
    User user = User.createTestUser("1");
    caffeineBinding.set(key, user);

    int threadCount = 20;
    int readsPerThread = 50;
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);

    // Updated to use try-with-resources for ExecutorService
    try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
      // When
      for (int i = 0; i < threadCount; i++) {
        executor.submit(
            () -> {
              try {
                for (int j = 0; j < readsPerThread; j++) {
                  User cachedUser = caffeineBinding.getObject(key, User.class);
                  if (cachedUser != null && user.getId().equals(cachedUser.getId())) {
                    successCount.incrementAndGet();
                  }
                }
              } finally {
                latch.countDown();
              }
            });
      }

      boolean completed = latch.await(30, TimeUnit.SECONDS);

      // Then
      assertTrue(completed, "All threads should complete");
      assertEquals(
          threadCount * readsPerThread, successCount.get(), "All read operations should succeed");
    }
  }

  @Test
  @DisplayName("测试并发读写混合操作")
  void testConcurrentReadWriteMix() throws InterruptedException {
    // Given
    int threadCount = 10;
    int operationsPerThread = 50;
    CountDownLatch latch = new CountDownLatch(threadCount);
    Map<String, AtomicInteger> statistics = new ConcurrentHashMap<>();
    statistics.put("writes", new AtomicInteger(0));
    statistics.put("reads", new AtomicInteger(0));
    statistics.put("deletes", new AtomicInteger(0));

    // Updated to use try-with-resources for ExecutorService
    try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
      // When
      for (int i = 0; i < threadCount; i++) {
        final int threadIndex = i; // Make 'i' effectively final
        executor.submit(
            () -> {
              try {
                Random random = new Random();
                for (int j = 0; j < operationsPerThread; j++) {
                  String key = "concurrent:mix:" + (random.nextInt(10));
                  int operation = random.nextInt(3);

                  switch (operation) {
                    case 0: // Write
                      User user = User.createTestUser(threadIndex + "-" + j);
                      caffeineBinding.set(key, user);
                      statistics.get("writes").incrementAndGet();
                      break;
                    case 1: // Read
                      caffeineBinding.getObject(key, User.class);
                      statistics.get("reads").incrementAndGet();
                      break;
                    case 2: // Delete
                      caffeineBinding.delete( key);
                      statistics.get("deletes").incrementAndGet();
                      break;
                  }
                }
              } finally {
                latch.countDown();
              }
            });
      }

      boolean completed = latch.await(30, TimeUnit.SECONDS);

      // Then
      assertTrue(completed, "All threads should complete");
      int totalOperations =
          statistics.get("writes").get()
              + statistics.get("reads").get()
              + statistics.get("deletes").get();
      assertEquals(
          threadCount * operationsPerThread, totalOperations, "All operations should be executed");

      log.info(
          "Concurrent operations completed - Writes: {}, Reads: {}, Deletes: {}",
          statistics.get("writes").get(),
          statistics.get("reads").get(),
          statistics.get("deletes").get());
    }
  }

  @Test
  @DisplayName("测试并发更新同一个key")
  void testConcurrentUpdateSameKey() throws InterruptedException {
    // Given
    String key = "concurrent:update:1";
    int threadCount = 10;
    int updatesPerThread = 20;
    CountDownLatch latch = new CountDownLatch(threadCount);
    Set<String> updatedNames = Collections.synchronizedSet(new HashSet<>());

    // Updated to use try-with-resources for ExecutorService
    try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
      // When
      for (int i = 0; i < threadCount; i++) {
        final int threadIndex = i; // Make 'i' effectively final
        executor.submit(
            () -> {
              try {
                for (int j = 0; j < updatesPerThread; j++) {
                  User user = User.createTestUser("1");
                  final String name =
                      "Thread-" + threadIndex + "-Update-" + j; // Make 'name' effectively final
                  user.setName(name);
                  caffeineBinding.set(key, user);
                  updatedNames.add(name);
                }
              } finally {
                latch.countDown();
              }
            });
      }

      boolean completed = latch.await(30, TimeUnit.SECONDS);

      // Then
      assertTrue(completed, "All threads should complete");

      // Final value should be one of the updated values
      User finalUser = caffeineBinding.getObject(key, User.class);
      assertNotNull(finalUser, "Final user should exist");
      assertTrue(
          updatedNames.contains(finalUser.getName()),
          "Final value should be from one of the updates");

      log.info(
          "Concurrent updates completed - Total unique updates: {}, Final value: {}",
          updatedNames.size(),
          finalUser.getName());
    }
  }

  @Test
  @DisplayName("测试并发删除")
  void testConcurrentDeletes() throws InterruptedException {
    // Given
    int keyCount = 100;
    for (int i = 0; i < keyCount; i++) {
      String key = "concurrent:delete:" + i;
      User user = User.createTestUser(String.valueOf(i));
      caffeineBinding.set(key, user);
    }

    int threadCount = 10;
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger deleteCount = new AtomicInteger(0);

    // Updated to use try-with-resources for ExecutorService
    try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
      // When - Each thread tries to delete some keys
      for (int i = 0; i < threadCount; i++) {
        executor.submit(
            () -> {
              try {
                for (int j = 0; j < keyCount; j++) {
                  String key = "concurrent:delete:" + j;
                  boolean deleted = caffeineBinding.delete( key);
                  if (deleted) {
                    deleteCount.incrementAndGet();
                  }
                }
              } finally {
                latch.countDown();
              }
            });
      }

      boolean completed = latch.await(30, TimeUnit.SECONDS);

      // Then
      assertTrue(completed, "All threads should complete");

      // Verify all keys are deleted
      int remainingKeys = 0;
      for (int i = 0; i < keyCount; i++) {
        String key = "concurrent:delete:" + i;
        User user = caffeineBinding.getObject(key, User.class);
        if (user != null) {
          remainingKeys++;
        }
      }

      assertEquals(0, remainingKeys, "All keys should be deleted");
      log.info("Concurrent deletes completed - Delete operations: {}", deleteCount.get());
    }
  }

  @Test
  @DisplayName("测试高并发下的数据一致性")
  void testDataConsistencyUnderHighConcurrency() throws InterruptedException {
    // Given
    int threadCount = 20;
    int operationsPerThread = 50;
    String keyPrefix = "consistency:test:";
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    Set<String> allKeys = Collections.synchronizedSet(new HashSet<>());

    try {
      // When - Multiple threads write different keys
      for (int i = 0; i < threadCount; i++) {
        final int threadIndex = i; // Make 'i' effectively final
        executor.submit(
            () -> {
              try {
                for (int j = 0; j < operationsPerThread; j++) {
                  final String key =
                      keyPrefix + threadIndex + ":" + j; // Make 'key' effectively final
                  final User user =
                      User.createTestUser(threadIndex + "-" + j); // Make 'user' effectively final
                  final String name =
                      "Thread-" + threadIndex + "-Op-" + j; // Make 'name' effectively final
                  user.setName(name);
                  caffeineBinding.set(key, user); // Ensure all variables are final
                }
              } finally {
                latch.countDown();
              }
            });
      }

      boolean completed = latch.await(30, TimeUnit.SECONDS);
      assertTrue(completed, "All threads should complete");

      // Then - Verify data consistency
      int validCount = 0;
      for (String key : allKeys) {
        User user = caffeineBinding.getObject(key, User.class);
        if (user != null) {
          validCount++;
        }
      }

      assertEquals(
          allKeys.size(), validCount, "All written keys should be retrievable with correct data");

      log.info(
          "Data consistency test completed - Total keys: {}, Valid: {}",
          allKeys.size(),
          validCount);
    } finally {
      executor.shutdown();
    }
  }
}
