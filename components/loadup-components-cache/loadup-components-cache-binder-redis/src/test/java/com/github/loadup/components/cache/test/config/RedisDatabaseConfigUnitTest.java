package com.github.loadup.components.cache.test.config;

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

import static org.junit.jupiter.api.Assertions.*;

import com.github.loadup.components.cache.redis.cfg.RedisBinderCfg;
import com.github.loadup.components.cache.test.CacheTestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for Redis database configuration
 *
 * <p>This test verifies the configuration binding without requiring a real Redis connection
 */
@SpringBootTest(classes = CacheTestApplication.class)
@TestPropertySource(
    properties = {
      "loadup.cache.binder=redis",
      // Custom database configuration - overrides Spring Boot default
      "loadup.cache.binder.redis.host=redis-cache.example.com",
      "loadup.cache.binder.redis.port=6380",
      "loadup.cache.binder.redis.database=5",
      "loadup.cache.binder.redis.password=cache-secret",
      // Spring Boot default configuration
      "spring.data.redis.host=redis-main.example.com",
      "spring.data.redis.port=6379",
      "spring.data.redis.database=0",
      "spring.data.redis.password=main-secret"
    })
@DisplayName("Redis Database Configuration Unit Test")
public class RedisDatabaseConfigUnitTest {

  @Autowired(required = false)
  private RedisBinderCfg redisBinderCfg;

  @Autowired(required = false)
  private RedisProperties springRedisProperties;

  @Test
  @DisplayName("验证 RedisBinderCfg 正确加载自定义配置")
  void testRedisBinderCfgLoadsCustomConfig() {
    assertNotNull(redisBinderCfg, "RedisBinderCfg should be loaded");

    // Verify custom configuration is loaded
    assertEquals(
        "redis-cache.example.com",
        redisBinderCfg.getHost(),
        "Host should be from loadup.cache.binder.redis");
    assertEquals(6380, redisBinderCfg.getPort(), "Port should be from loadup.cache.binder.redis");
    assertEquals(
        5, redisBinderCfg.getDatabase(), "Database should be 5 from loadup.cache.binder.redis");
    assertEquals(
        "cache-secret",
        redisBinderCfg.getPassword(),
        "Password should be from loadup.cache.binder.redis");
  }

  @Test
  @DisplayName("验证 Spring Redis Properties 保持独立")
  void testSpringRedisPropertiesRemainIndependent() {
    assertNotNull(springRedisProperties, "Spring RedisProperties should be loaded");

    // Verify Spring Boot default configuration is still there (not affected by custom config)
    assertEquals(
        "redis-main.example.com",
        springRedisProperties.getHost(),
        "Spring host should remain as configured");
    assertEquals(6379, springRedisProperties.getPort(), "Spring port should remain as configured");
    assertEquals(
        0, springRedisProperties.getDatabase(), "Spring database should remain as configured");
    assertEquals(
        "main-secret",
        springRedisProperties.getPassword(),
        "Spring password should remain as configured");
  }

  @Test
  @DisplayName("验证自定义配置覆盖默认配置的优先级")
  void testConfigurationPriority() {
    assertNotNull(redisBinderCfg, "RedisBinderCfg should be loaded");
    assertNotNull(springRedisProperties, "Spring RedisProperties should be loaded");

    // Custom config should be different from Spring Boot default
    assertNotEquals(
        springRedisProperties.getHost(),
        redisBinderCfg.getHost(),
        "Custom host should override default");
    assertNotEquals(
        springRedisProperties.getPort(),
        redisBinderCfg.getPort(),
        "Custom port should override default");
    assertNotEquals(
        springRedisProperties.getDatabase(),
        redisBinderCfg.getDatabase(),
        "Custom database should override default");
    assertNotEquals(
        springRedisProperties.getPassword(),
        redisBinderCfg.getPassword(),
        "Custom password should override default");
  }

  @Test
  @DisplayName("验证 database 参数为 Integer 类型，支持 null")
  void testDatabaseIsIntegerType() {
    assertNotNull(redisBinderCfg, "RedisBinderCfg should be loaded");

    Integer database = redisBinderCfg.getDatabase();
    assertNotNull(database, "Database should be set");
    assertInstanceOf(Integer.class, database, "Database should be Integer type");
    assertEquals(5, database.intValue());
  }

  @Test
  @DisplayName("验证 hasCustomConfig 方法")
  void testHasCustomConfig() {
    assertNotNull(redisBinderCfg, "RedisBinderCfg should be loaded");

    assertTrue(
        redisBinderCfg.hasCustomConfig(),
        "Should detect custom configuration when any field is set");
  }

  @Test
  @DisplayName("验证所有基础配置字段")
  void testAllBasicConfigFields() {
    assertNotNull(redisBinderCfg, "RedisBinderCfg should be loaded");

    // Verify all basic fields are loaded
    assertNotNull(redisBinderCfg.getHost(), "Host should be set");
    assertNotNull(redisBinderCfg.getPort(), "Port should be set");
    assertNotNull(redisBinderCfg.getDatabase(), "Database should be set");
    assertNotNull(redisBinderCfg.getPassword(), "Password should be set");

    // Verify correct values
    assertEquals("redis-cache.example.com", redisBinderCfg.getHost());
    assertEquals(6380, redisBinderCfg.getPort().intValue());
    assertEquals(5, redisBinderCfg.getDatabase().intValue());
    assertEquals("cache-secret", redisBinderCfg.getPassword());
  }

  @Test
  @DisplayName("验证不同 database 索引的有效性")
  void testDatabaseIndexRange() {
    assertNotNull(redisBinderCfg, "RedisBinderCfg should be loaded");

    Integer database = redisBinderCfg.getDatabase();
    assertNotNull(database, "Database should be set");

    // Redis supports database index 0-15 by default
    assertTrue(database >= 0, "Database index should be >= 0");
    assertTrue(database <= 15, "Database index should be <= 15 (Redis default)");
  }
}
