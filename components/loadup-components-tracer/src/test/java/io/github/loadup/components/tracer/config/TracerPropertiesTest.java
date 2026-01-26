package io.github.loadup.components.tracer.config;

/*-
 * #%L
 * loadup-components-tracer
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

/** Tests for TracerProperties validation */
class TracerPropertiesTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testValidOtlpEndpoint() {
    TracerProperties properties = new TracerProperties();
    properties.setOtlpEndpoint("http://localhost:4317");

    Set<ConstraintViolation<TracerProperties>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Valid OTLP endpoint should have no violations");
  }

  @Test
  void testValidHttpsOtlpEndpoint() {
    TracerProperties properties = new TracerProperties();
    properties.setOtlpEndpoint("https://otel-collector.example.com:4317");

    Set<ConstraintViolation<TracerProperties>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Valid HTTPS OTLP endpoint should have no violations");
  }

  @Test
  void testValidOtlpEndpointWithPath() {
    TracerProperties properties = new TracerProperties();
    properties.setOtlpEndpoint("http://localhost:4317/v1/traces");

    Set<ConstraintViolation<TracerProperties>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Valid OTLP endpoint with path should have no violations");
  }

  @Test
  void testValidOtlpEndpointWithoutScheme() {
    TracerProperties properties = new TracerProperties();
    properties.setOtlpEndpoint("localhost:4317");

    Set<ConstraintViolation<TracerProperties>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "OTLP endpoint without scheme should be valid");
  }

  @Test
  void testInvalidOtlpEndpoint() {
    TracerProperties properties = new TracerProperties();
    properties.setOtlpEndpoint("not a valid url!");

    Set<ConstraintViolation<TracerProperties>> violations = validator.validate(properties);
    assertFalse(violations.isEmpty(), "Invalid OTLP endpoint should have violations");
    assertTrue(
        violations.stream()
            .anyMatch(v -> v.getMessage().contains("OTLP endpoint must be a valid URL format")),
        "Should have URL validation message");
  }

  @Test
  void testNullOtlpEndpoint() {
    TracerProperties properties = new TracerProperties();
    properties.setOtlpEndpoint(null);

    Set<ConstraintViolation<TracerProperties>> violations = validator.validate(properties);
    assertTrue(violations.isEmpty(), "Null OTLP endpoint should be valid (optional)");
  }

  @Test
  void testDefaultValues() {
    TracerProperties properties = new TracerProperties();

    assertTrue(properties.isEnabled());
    assertTrue(properties.isEnableWebTracing());
    assertTrue(properties.isEnableAsyncTracing());
    assertFalse(properties.isIncludeHeaders());
    assertFalse(properties.isIncludeParameters());
    assertEquals("/actuator/**,/health,/metrics", properties.getExcludePatterns());
  }
}
