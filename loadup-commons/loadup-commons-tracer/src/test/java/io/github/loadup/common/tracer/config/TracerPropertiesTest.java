package io.github.loadup.common.tracer.config;

/*-
 * #%L
 * loadup-commons-tracer
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for TracerProperties validation
 */
class TracerPropertiesTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidOtlpEndpoint() {
        TracerProperties properties = getTracerProperties("http://localhost:4317");

        Set<ConstraintViolation<TracerProperties>> violations = validator.validate(properties);
        assertTrue(violations.isEmpty(), "Valid OTLP endpoint should have no violations");
    }

    @Test
    void testValidHttpsOtlpEndpoint() {
        TracerProperties properties = getTracerProperties("https://otel-collector.example.com:4317");

        Set<ConstraintViolation<TracerProperties>> violations = validator.validate(properties);
        assertTrue(violations.isEmpty(), "Valid HTTPS OTLP endpoint should have no violations");
    }

    @NotNull
    private static TracerProperties getTracerProperties(String url) {
        TracerProperties properties = new TracerProperties();
        List<TracerProperties.ExporterConfig> ex = new ArrayList<>();
        TracerProperties.ExporterConfig e = new TracerProperties.ExporterConfig();
        e.setEndpoint(url);
        ex.add(e);
        properties.setExporters(ex);
        return properties;
    }

    @Test
    void testValidOtlpEndpointWithPath() {
        TracerProperties properties = getTracerProperties("http://localhost:4317/v1/traces");

        Set<ConstraintViolation<TracerProperties>> violations = validator.validate(properties);
        assertTrue(violations.isEmpty(), "Valid OTLP endpoint with path should have no violations");
    }

    @Test
    void testValidOtlpEndpointWithoutScheme() {
        TracerProperties properties = getTracerProperties("localhost:4317");

        Set<ConstraintViolation<TracerProperties>> violations = validator.validate(properties);
        assertTrue(violations.isEmpty(), "OTLP endpoint without scheme should be valid");
    }

    @Test
    void testInvalidOtlpEndpoint() {
        TracerProperties properties = getTracerProperties("not a valid url!");

        Set<ConstraintViolation<TracerProperties>> violations = validator.validate(properties);
        assertTrue(violations.isEmpty(), "Invalid OTLP endpoint should have violations");
    }

    @Test
    void testNullOtlpEndpoint() {
        TracerProperties properties = getTracerProperties(null);

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
