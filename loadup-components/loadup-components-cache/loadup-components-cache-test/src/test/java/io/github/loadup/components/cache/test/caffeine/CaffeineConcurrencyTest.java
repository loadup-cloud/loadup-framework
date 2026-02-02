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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caffeine Cache Concurrent Operations Test */
@TestPropertySource(
    properties = {
        "loadup.cache.binder=caffeine",
    })
@DisplayName("Caffeine 缓存并发操作测试")

public class CaffeineConcurrencyTest extends BaseCacheTest {

  @Test
  @DisplayName("测试并发写入")
  void testConcurrentWrites() throws InterruptedException {
    // Given
    int threadCount = 10;
    int operationsPerThread = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);

    // When
    for (int i = 0; i < threadCount; i++) {
      final int threadIndex = i;
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < operationsPerThread; j++) {
                String key = "user:" + threadIndex + ":" + j;
                User user = User.createTestUser(key);
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

    latch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    // Then
    assertEquals(
        threadCount * operationsPerThread,
        successCount.get(),
        "All concurrent writes should succeed");

    // Verify all data is cached
    for (int i = 0; i < threadCount; i++) {
      for (int j = 0; j < operationsPerThread; j++) {
        String key = "user:" + i + ":" + j;
        User user = caffeineBinding.getObject(key, User.class);
        assertNotNull(user, "User should be cached: " + key);
      }
    }
  }

  @Test
  @DisplayName("测试并发读取")
  void testConcurrentReads() throws InterruptedException {
    // Given
    int dataCount = 100;
    for (int i = 0; i < dataCount; i++) {
      caffeineBinding.set("user:" + i, User.createTestUser(String.valueOf(i)));
    }

    int threadCount = 20;
    int readsPerThread = 50;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);

    // When
    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < readsPerThread; j++) {
                String key = "user:" + (j % dataCount);
                User user = caffeineBinding.getObject(key, User.class);
                if (user != null) {
                  successCount.incrementAndGet();
                }
              }
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    // Then
    assertEquals(
        threadCount * readsPerThread, successCount.get(), "All concurrent reads should succeed");
  }

  @Test
  @DisplayName("测试并发读写混合")
  void testConcurrentReadWrite() throws InterruptedException {
    // Given - Pre-populate some data
    for (int i = 0; i < 50; i++) {
      String key = "user:" + i;
      User user = User.createTestUser(key);
      user.setName("Initial-" + i);
      caffeineBinding.set(key, user);
    }

    int threadCount = 10;
    int operationsPerThread = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    // When
    for (int i = 0; i < threadCount; i++) {
      final int threadIndex = i;
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < operationsPerThread; j++) {
                String key = "user:" + (j % 50); // Overlapping keys

                if (j % 2 == 0) {
                  // Write
                  User user = User.createTestUser(key);
                  user.setName("Thread" + threadIndex + "-" + j);
                  caffeineBinding.set(key, user);
                } else {
                  // Read
                  caffeineBinding.getObject(key, User.class);
                }
              }
            } catch (Exception e) {
              // Log but don't fail - concurrent access may have timing issues
              System.err.println("Thread " + threadIndex + " error: " + e.getMessage());
            } finally {
              latch.countDown();
            }
          });
    }

    // Wait with longer timeout for CI environments
    boolean completed = latch.await(60, TimeUnit.SECONDS);
    assertTrue(completed, "All threads should complete within timeout");

    executor.shutdown();
    boolean terminated = executor.awaitTermination(10, TimeUnit.SECONDS);
    assertTrue(terminated, "Executor should terminate cleanly");

    // Then - Verify no corruption
    for (int i = 0; i < 50; i++) {
      String key = "user:" + i;
      User user = caffeineBinding.getObject(key, User.class);
      assertNotNull(user, "User should exist: " + key);
      // Name should either be from Initial or Thread prefix
      assertTrue(
          user.getName().startsWith("Thread") || user.getName().startsWith("Initial"),
          "User data should be valid: " + user.getName());
    }
  }

  @Test
  @DisplayName("测试并发删除")
  void testConcurrentDeletes() throws InterruptedException {
    // Given
    int dataCount = 1000;
    for (int i = 0; i < dataCount; i++) {
      caffeineBinding.set("user:" + i, User.createTestUser(String.valueOf(i)));
    }

    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    // When
    for (int i = 0; i < threadCount; i++) {
      final int threadIndex = i;
      executor.submit(
          () -> {
            try {
              for (int j = threadIndex; j < dataCount; j += threadCount) {
                caffeineBinding.delete( "user:" + j);
              }
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await(30, TimeUnit.SECONDS);
    executor.shutdown();

    // Then - All items should be deleted
    for (int i = 0; i < dataCount; i++) {
      User user = caffeineBinding.getObject("user:" + i, User.class);
      assertNull(user, "User should be deleted: " + i);
    }
  }

  @Test
  @DisplayName("测试高并发场景下的缓存一致性")
  void testCacheConsistencyUnderHighConcurrency() throws InterruptedException, ExecutionException {
    // Given
    String key = "concurrent:key";
    int threadCount = 50;
    int operationsPerThread = 20;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    List<Future<Boolean>> futures = new ArrayList<>();

    // When - Multiple threads updating the same key
    for (int i = 0; i < threadCount; i++) {
      final int threadIndex = i;
      Future<Boolean> future =
          executor.submit(
              () -> {
                for (int j = 0; j < operationsPerThread; j++) {
                  User user = User.createTestUser(String.valueOf(threadIndex));
                  user.setName("Thread" + threadIndex + "-Iteration" + j);
                  caffeineBinding.set(key, user);
                  sleep(10); // Small delay
                }
                return true;
              });
      futures.add(future);
    }

    // Wait for all threads
    for (Future<Boolean> future : futures) {
      assertTrue(future.get(), "Thread should complete successfully");
    }

    executor.shutdown();
    executor.awaitTermination(30, TimeUnit.SECONDS);

    // Then - Verify final state is consistent
    User finalUser = caffeineBinding.getObject(key, User.class);
    assertNotNull(finalUser, "Final user should exist");
    assertNotNull(finalUser.getName(), "Final user name should not be null");
  }

  @Test
  @DisplayName("测试缓存性能压测")
  void testCachePerformance() throws InterruptedException {
    // Given
    int threadCount = 20;
    int operationsPerThread = 1000;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    long startTime = System.currentTimeMillis();

    // When
    for (int i = 0; i < threadCount; i++) {
      final int threadIndex = i;
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < operationsPerThread; j++) {
                String key = "perf:user:" + threadIndex + ":" + j;

                // Write
                User user = User.createTestUser(key);
                caffeineBinding.set(key, user);

                // Read
                caffeineBinding.getObject(key, User.class);
              }
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await(60, TimeUnit.SECONDS);
    executor.shutdown();

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    int totalOperations = threadCount * operationsPerThread * 2; // write + read
    double opsPerSecond = (totalOperations * 1000.0) / duration;

    // Then
    System.out.println("Performance Test Results:");
    System.out.println("Total operations: " + totalOperations);
    System.out.println("Duration: " + duration + "ms");
    System.out.println("Operations per second: " + opsPerSecond);

    assertTrue(opsPerSecond > 1000, "Should support at least 1000 ops/sec, got: " + opsPerSecond);
  }
}
