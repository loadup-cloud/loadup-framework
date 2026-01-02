package com.github.loadup.components.dfs.config;

/*-
 * #%L
 * loadup-components-dfs-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

/** Tests for DfsProperties validation */
class DfsPropertiesTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testValidMaxFileSize() {
    DfsProperties properties = new DfsProperties();
    properties.setMaxFileSize(100 * 1024 * 1024L); // 100MB

    Set<ConstraintViolation<DfsProperties>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Valid configuration should have no violations");
  }

  @Test
  void testNullMaxFileSize() {
    DfsProperties properties = new DfsProperties();
    properties.setMaxFileSize(null);

    Set<ConstraintViolation<DfsProperties>> violations = validator.validate(properties);
    assertFalse(violations.isEmpty(), "Null maxFileSize should be invalid");
    assertTrue(
        violations.stream()
            .anyMatch(v -> v.getMessage().contains("Maximum file size cannot be null")),
        "Should have null validation message");
  }

  @Test
  void testMaxFileSizeTooSmall() {
    DfsProperties properties = new DfsProperties();
    properties.setMaxFileSize(0L);

    Set<ConstraintViolation<DfsProperties>> violations = validator.validate(properties);
    assertFalse(violations.isEmpty(), "Zero maxFileSize should be invalid");
    assertTrue(
        violations.stream()
            .anyMatch(v -> v.getMessage().contains("Maximum file size must be at least 1 byte")),
        "Should have min value validation message");
  }

  @Test
  void testMaxFileSizeMinValue() {
    DfsProperties properties = new DfsProperties();
    properties.setMaxFileSize(1L);

    Set<ConstraintViolation<DfsProperties>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Min value of 1 byte should be valid");
  }

  @Test
  void testDefaultValues() {
    DfsProperties properties = new DfsProperties();
    
    assertEquals("local", properties.getDefaultProvider());
    assertEquals(100 * 1024 * 1024L, properties.getMaxFileSize());
    assertNotNull(properties.getProviders());
  }
}
