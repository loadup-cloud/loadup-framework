package com.github.loadup.components.cache.test.caffeine;

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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import com.github.loadup.components.cache.test.common.BaseCacheTest;
import com.github.loadup.components.cache.test.common.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Anti-Avalanche and Anti-Breakdown Test 测试防缓存雪崩和缓存击穿策略 */
@TestPropertySource(
    properties = {
      "loadup.cache.binder=caffeine",
      "loadup.cache.binders.caffeine.expireAfterWrite=10s",
      "loadup.cache.binders.caffeine.enableRandomExpiry=true",
      "loadup.cache.binders.caffeine.randomFactor=0.2"
    })
@DisplayName("Caffeine 防缓存雪崩和击穿测试")
@Slf4j
public class CaffeineAntiAvalancheTest extends BaseCacheTest {

  @Test
  @DisplayName("测试随机过期时间防止缓存雪崩")
  @Order(1)
  void testRandomExpirationPreventsAvalanche() {
    // Given - 同时缓存多个数据项
    int itemCount = 10;
    List<String> keys = new ArrayList<>();

    // When - 在同一时刻缓存所有数据
    for (int i = 0; i < itemCount; i++) {
      String key = "user:" + i;
      keys.add(key);
      caffeineBinding.set(key, User.createTestUser(String.valueOf(i)));
    }

    // Then - 验证过期时间是分散的
    // 在基础过期时间（10s）处，不应该所有缓存都失效
    fakeTicker.advance(11, TimeUnit.SECONDS);

    int stillCachedCount = 0;
    for (String key : keys) {
      User user = caffeineBinding.getObject(key, User.class);
      if (user != null) {
        stillCachedCount++;
      }
    }

    // 由于随机偏移（10*0.2=2秒），应该有部分数据还未过期
    log.info("After 10s, still cached: {} out of {}", stillCachedCount, itemCount);
    assertTrue(stillCachedCount > 0, "Some items should still be cached due to random expiration");
    assertTrue(
        stillCachedCount <= 10, "Some items should still be cached due to random expiration");

    // 等待所有数据都过期（10s + 2s random offset ）
    fakeTicker.advance(2, TimeUnit.SECONDS);
    int finalCachedCount = 0;

    for (String key : keys) {
      User user = caffeineBinding.getObject(key, User.class);
      if (user != null) {
        finalCachedCount++;
      }
    }
    assertEquals(finalCachedCount, 0, "All items should be expired after max random offset");
  }

  @Test
  @DisplayName("测试批量数据的分散过期")
  void testBatchDataStaggeredExpiration() {
    int batchSize = 100;

    // 2. 构建缓存，模拟分散过期的逻辑
    // 3. 批量添加数据 (时间点 0)
    for (int i = 0; i < batchSize; i++) {
      caffeineBinding.set("batch:user:" + i, "data-" + i);
    }

    // 4. 验证过期分布
    List<Integer> expiredCountAtPoints = new ArrayList<>();

    // 我们按 100ms 的步长拨动时钟
    for (int i = 0; i < 120; i++) { // 模拟 12 秒的时间跨度
      fakeTicker.advance(100, TimeUnit.MILLISECONDS);
      caffeineBinding.cleanUp(); // 强制执行维护任务（关键：Caffeine 是惰性过期的）

      int currentCount = 0;
      for (int j = 0; j < batchSize; j++) {
        if (caffeineBinding.get("batch:user:" + j) != null) {
          currentCount++;
        }
      }
      expiredCountAtPoints.add(batchSize - currentCount);
    }

    // 5. 断言验证
    long firstExpirationPoint =
        expiredCountAtPoints.stream().filter(c -> c > 0).findFirst().orElse(0);
    long lastExpirationPoint = expiredCountAtPoints.get(expiredCountAtPoints.size() - 1);

    System.out.println("过期数量随时间的变化: " + expiredCountAtPoints);

    // 验证：数据不是在同一时刻全部消失的
    // 如果 expiredCountAtPoints 中包含多个不同的数值，说明是阶梯式过期的
    long distinctStates = expiredCountAtPoints.stream().distinct().count();

    assertTrue(distinctStates > 2, "缓存应该是分批过期的，而不应只有“全有”和“全无”两个状态");
    assertTrue(lastExpirationPoint == batchSize, "最终所有数据都应该过期");
  }
}
