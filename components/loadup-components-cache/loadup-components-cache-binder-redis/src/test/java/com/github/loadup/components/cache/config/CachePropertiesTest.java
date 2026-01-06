package com.github.loadup.components.cache.config;

/*-
 * #%L
 * loadup-components-cache-api
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

import com.github.loadup.components.cache.cfg.LoadUpCacheConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for CacheProperties validation and configuration */
class CachePropertiesTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testValidCaffeineConfig() {
    CacheProperties properties = new CacheProperties();
    properties.setType(CacheProperties.CacheType.CAFFEINE);

    Set<ConstraintViolation<CacheProperties>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Valid configuration should have no violations");
  }

  @Test
  void testValidRedisConfig() {
    CacheProperties properties = new CacheProperties();
    properties.setType(CacheProperties.CacheType.REDIS);

    Set<ConstraintViolation<CacheProperties>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Valid configuration should have no violations");
  }

  @Test
  void testDefaultValues() {
    CacheProperties properties = new CacheProperties();

    assertEquals(CacheProperties.CacheType.CAFFEINE, properties.getType());
    assertNotNull(properties.getRedis());
    assertNotNull(properties.getCaffeine());
  }

  @Test
  void testCacheTypeEnum() {
    assertEquals("redis", CacheProperties.CacheType.REDIS.toString());
    assertEquals("caffeine", CacheProperties.CacheType.CAFFEINE.toString());
  }

  @Test
  void testPerCacheConfiguration() {
    CacheProperties properties = new CacheProperties();

    // Configure Redis per-cache settings
    Map<String, LoadUpCacheConfig> redisCacheConfig = new HashMap<>();
    LoadUpCacheConfig userCacheConfig = new LoadUpCacheConfig();
    userCacheConfig.setExpireAfterWrite("30m");
    userCacheConfig.setMaximumSize(10000L);
    redisCacheConfig.put("users", userCacheConfig);
    properties.getRedis().setCacheConfig(redisCacheConfig);

    assertNotNull(properties.getRedis().getCacheConfig());
    assertEquals(1, properties.getRedis().getCacheConfig().size());
    assertEquals("30m", properties.getRedis().getCacheConfig().get("users").getExpireAfterWrite());
  }
}
