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

import com.github.loadup.components.cache.cfg.CacheBindingCfg;
import com.github.loadup.components.cache.cfg.LoadUpCacheConfig;
import com.github.loadup.framework.api.enums.BinderEnum;
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
    CacheBindingCfg properties = new CacheBindingCfg();
    properties.setBinder(BinderEnum.CacheBinder.CAFFEINE);

    Set<ConstraintViolation<CacheBindingCfg>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Valid configuration should have no violations");
  }

  @Test
  void testValidRedisConfig() {
    CacheBindingCfg properties = new CacheBindingCfg();
    properties.setBinder(BinderEnum.CacheBinder.REDIS);

    Set<ConstraintViolation<CacheBindingCfg>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Valid configuration should have no violations");
  }

  @Test
  void testDefaultValues() {
    CacheBindingCfg properties = new CacheBindingCfg();

    // Default binder should be CAFFEINE when not set
    assertEquals(BinderEnum.CacheBinder.CAFFEINE, properties.getBinderForCache("anyCache"));
    assertNotNull(properties.getBinders());
    assertNotNull(properties.getCacheConfigs());
  }


  @Test
  void testPerCacheConfiguration() {
    CacheBindingCfg properties = new CacheBindingCfg();

    // Configure per-cache settings
    Map<String, LoadUpCacheConfig> cacheConfigs = new HashMap<>();
    LoadUpCacheConfig userCacheConfig = new LoadUpCacheConfig();
    userCacheConfig.setExpireAfterWrite("30m");
    userCacheConfig.setMaximumSize(10000L);
    cacheConfigs.put("users", userCacheConfig);
    properties.setCacheConfigs(cacheConfigs);

    assertNotNull(properties.getCacheConfigs());
    assertEquals(1, properties.getCacheConfigs().size());
    assertEquals("30m", properties.getCacheConfig("users").getExpireAfterWrite());
  }

  @Test
  void testNewConfigurationStructure() {
    CacheBindingCfg properties = new CacheBindingCfg();

    // Set global binder
    properties.setBinder(BinderEnum.CacheBinder.REDIS);

    // Configure per-cache binders
    Map<String, BinderEnum.CacheBinder> binders = new HashMap<>();
    binders.put("userCache", BinderEnum.CacheBinder.REDIS);
    binders.put("productCache", BinderEnum.CacheBinder.CAFFEINE);
    properties.setBinders(binders);

    // Configure per-cache common properties
    Map<String, LoadUpCacheConfig> cacheConfigs = new HashMap<>();

    LoadUpCacheConfig userConfig = new LoadUpCacheConfig();
    userConfig.setExpireAfterWrite("30m");
    userConfig.setMaximumSize(10000L);
    userConfig.setEnableRandomExpiration(true);
    cacheConfigs.put("userCache", userConfig);

    LoadUpCacheConfig productConfig = new LoadUpCacheConfig();
    productConfig.setExpireAfterWrite("1h");
    productConfig.setMaximumSize(5000L);
    cacheConfigs.put("productCache", productConfig);

    properties.setCacheConfigs(cacheConfigs);

    // Verify configuration
    assertEquals(BinderEnum.CacheBinder.REDIS, properties.getBinder());
    assertEquals(BinderEnum.CacheBinder.REDIS, properties.getBinderForCache("userCache"));
    assertEquals(BinderEnum.CacheBinder.CAFFEINE, properties.getBinderForCache("productCache"));
    assertEquals(
        BinderEnum.CacheBinder.REDIS,
        properties.getBinderForCache("unknownCache")); // Falls back to global

    assertNotNull(properties.getCacheConfig("userCache"));
    assertEquals("30m", properties.getCacheConfig("userCache").getExpireAfterWrite());
    assertEquals(10000L, properties.getCacheConfig("userCache").getMaximumSize());

    assertNotNull(properties.getCacheConfig("productCache"));
    assertEquals("1h", properties.getCacheConfig("productCache").getExpireAfterWrite());
    assertEquals(5000L, properties.getCacheConfig("productCache").getMaximumSize());
  }

  @Test
  void testBinderFallbackToGlobal() {
    CacheBindingCfg properties = new CacheBindingCfg();
    properties.setBinder(BinderEnum.CacheBinder.REDIS);

    // No specific binder for "userCache", should fall back to global "redis"
    assertEquals(BinderEnum.CacheBinder.REDIS, properties.getBinderForCache("userCache"));

    // Set specific binder for "productCache"
    Map<String, BinderEnum.CacheBinder> binders = new HashMap<>();
    binders.put("productCache", BinderEnum.CacheBinder.CAFFEINE);
    properties.setBinders(binders);

    assertEquals(BinderEnum.CacheBinder.CAFFEINE, properties.getBinderForCache("productCache"));
    assertEquals(
        BinderEnum.CacheBinder.REDIS,
        properties.getBinderForCache("userCache")); // Still falls back
  }

  @Test
  void testDefaultBinderValue() {
    CacheProperties properties = new CacheProperties();

    // Default binder should be "caffeine"
    assertEquals("caffeine", properties.getBinder());
  }
}
