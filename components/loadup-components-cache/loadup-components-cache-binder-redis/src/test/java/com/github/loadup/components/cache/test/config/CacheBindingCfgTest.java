package com.github.loadup.components.cache.test.config;

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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for CacheProperties validation and configuration */
class CacheBindingCfgTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testValidCaffeineConfig() {
    CacheBindingCfg properties = new CacheBindingCfg();

    Set<ConstraintViolation<CacheBindingCfg>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Valid configuration should have no violations");
  }

  @Test
  void testValidRedisConfig() {
    CacheBindingCfg properties = new CacheBindingCfg();

    Set<ConstraintViolation<CacheBindingCfg>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Valid configuration should have no violations");
  }
}
