package com.github.loadup.components.cache.test.redis;

/*-
 * #%L
 * loadup-components-cache-binder-redis
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import static com.github.loadup.components.testcontainers.cache.SharedRedisContainer.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.loadup.components.testcontainers.cache.SharedRedisContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.*;

/**
 * Test Redis database configuration
 *
 * <p>This test verifies that the Redis cache can use a custom database index via
 * loadup.cache.binder.redis.database configuration
 *
 * <p>"spring.datasource.url=" + SharedMySQLContainer.JDBC_URL, "spring.datasource.username=" +
 * SharedMySQLContainer.USERNAME, "spring.datasource.password=" + SharedMySQLContainer.PASSWORD
 */
@TestPropertySource(
    properties = {
      "loadup.cache.binder=redis",
      // Custom database configuration
      "loadup.cache.bindings.redis-biz-type.database=5",
    })
@DisplayName("Redis Database Configuration Test")
public class RedisDatabaseConfigIT extends BaseRedisCacheTest {

  @Autowired(required = false)
  private RedisConnectionFactory redisConnectionFactory;

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    // 这里的 HOST 可以是任何运行时的动态变量
    registry.add("loadup.cache.binder.redis.host", () -> SharedRedisContainer.getHost());
    registry.add("loadup.cache.binder.redis.port", () -> SharedRedisContainer.getPort());
    registry.add("spring.data.redis.host", () -> SharedRedisContainer.getHost());
    registry.add("spring.data.redis.port", () -> SharedRedisContainer.getPort());
    registry.add("spring.data.redis.database", () -> 5);
  }

  @Test
  @DisplayName("验证 RedisConnectionFactory 使用自定义 database")
  void testRedisConnectionFactoryUsesCustomDatabase() {
    assertNotNull(redisConnectionFactory, "RedisConnectionFactory should be created");

    // Verify it's LettuceConnectionFactory
    assertInstanceOf(
        LettuceConnectionFactory.class,
        redisConnectionFactory,
        "Should be LettuceConnectionFactory");

    LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) redisConnectionFactory;

    // Get the configuration
    RedisStandaloneConfiguration standaloneConfig = lettuceFactory.getStandaloneConfiguration();
    assertNotNull(standaloneConfig, "Standalone configuration should not be null");

    // Verify database is 5 (custom config) not 0 (Spring Boot default)
    assertEquals(
        5,
        standaloneConfig.getDatabase(),
        "Redis database should be 5 from loadup.cache.binder.redis.database");
    Assertions.assertEquals(SharedRedisContainer.getHost(), standaloneConfig.getHostName());
    Assertions.assertEquals(SharedRedisContainer.getPort(), standaloneConfig.getPort());
  }

  @Test
  @DisplayName("验证可以连接到指定的 database 并进行读写操作")
  void testRedisOperationsOnCustomDatabase() {
    assertNotNull(redisConnectionFactory, "RedisConnectionFactory should be available");

    // Create RedisTemplate
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.afterPropertiesSet();

    // Test key
    String testKey = "test:database:config:key";
    String testValue = "database-5-value";

    try {
      // Write to Redis
      redisTemplate.opsForValue().set(testKey, testValue);

      // Read from Redis
      String retrievedValue = redisTemplate.opsForValue().get(testKey);

      // Verify
      assertEquals(testValue, retrievedValue, "Should be able to read/write from database 5");

    } finally {
      // Cleanup
      redisTemplate.delete(testKey);
    }
  }

  @Test
  @DisplayName("验证数据隔离：database 5 的数据不会出现在 database 0")
  void testDatabaseIsolation() {
    assertNotNull(redisConnectionFactory, "RedisConnectionFactory should be available");

    // Create template for database 5 (custom config)
    RedisTemplate<String, String> db5Template = new RedisTemplate<>();
    db5Template.setConnectionFactory(redisConnectionFactory);
    db5Template.afterPropertiesSet();

    // Create template for database 0 (to verify isolation)
    LettuceConnectionFactory db0Factory =
        new LettuceConnectionFactory(
            new RedisStandaloneConfiguration(
                SharedRedisContainer.getHost(), SharedRedisContainer.getPort()));
    db0Factory.afterPropertiesSet();

    RedisTemplate<String, String> db0Template = new RedisTemplate<>();
    db0Template.setConnectionFactory(db0Factory);
    db0Template.afterPropertiesSet();

    String testKey = "test:isolation:key";
    String db5Value = "value-in-db5";

    try {
      // Write to database 5
      db5Template.opsForValue().set(testKey, db5Value);

      // Verify exists in database 5
      String db5Retrieved = db5Template.opsForValue().get(testKey);
      assertEquals(db5Value, db5Retrieved, "Value should exist in database 5");

      // Verify NOT exists in database 0
      String db0Retrieved = db0Template.opsForValue().get(testKey);
      assertNull(db0Retrieved, "Value should NOT exist in database 0 (data isolation)");

    } finally {
      // Cleanup
      db5Template.delete(testKey);
      db0Factory.destroy();
    }
  }
}
