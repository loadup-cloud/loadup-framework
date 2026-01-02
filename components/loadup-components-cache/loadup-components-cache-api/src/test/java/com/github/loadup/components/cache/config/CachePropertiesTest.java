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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for CacheProperties validation */
class CachePropertiesTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testValidRedisConfig() {
    CacheProperties properties = new CacheProperties();
    properties.getRedis().setHost("localhost");
    properties.getRedis().setPort(6379);

    Set<ConstraintViolation<CacheProperties>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Valid configuration should have no violations");
  }

  @Test
  void testBlankRedisHost() {
    CacheProperties properties = new CacheProperties();
    properties.getRedis().setHost("");

    Set<ConstraintViolation<CacheProperties>> violations = validator.validate(properties);
    assertFalse(violations.isEmpty(), "Blank host should be invalid");
    assertTrue(
        violations.stream()
            .anyMatch(v -> v.getMessage().contains("Redis host cannot be blank")),
        "Should have Redis host validation message");
  }

  @Test
  void testNullRedisHost() {
    CacheProperties properties = new CacheProperties();
    properties.getRedis().setHost(null);

    Set<ConstraintViolation<CacheProperties>> violations = validator.validate(properties);
    assertFalse(violations.isEmpty(), "Null host should be invalid");
  }

  @Test
  void testDefaultValues() {
    CacheProperties properties = new CacheProperties();
    
    assertEquals("caffeine", properties.getType());
    assertEquals("localhost", properties.getRedis().getHost());
    assertEquals(6379, properties.getRedis().getPort());
    assertEquals(0, properties.getRedis().getDatabase());
  }
}
