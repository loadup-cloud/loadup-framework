package com.github.loadup.components.tracer;

/*-
 * #%L
 * loadup-components-tracer
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

import com.github.loadup.components.tracer.config.TracerProperties;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for OpenTelemetryConfig.
 */
@SpringBootTest(classes = TestConfiguration.class)
@TestPropertySource(properties = {
    "spring.application.name=otel-test-service",
    "loadup.tracer.enabled=true",
    "loadup.tracer.enable-web-tracing=true",
    "loadup.tracer.enable-async-tracing=true",
    "loadup.tracer.include-headers=true",
    "loadup.tracer.include-parameters=true",
    "loadup.tracer.exclude-patterns=/test/**,/health"
})
class OpenTelemetryConfigTest {

    @Autowired
    private OpenTelemetry openTelemetry;

    @Autowired
    private Tracer tracer;

    @Autowired
    private TracerProperties tracerProperties;

    @Test
    void testOpenTelemetryBeanCreated() {
        assertThat(openTelemetry).isNotNull();
    }

    @Test
    void testTracerBeanCreated() {
        assertThat(tracer).isNotNull();
    }

    @Test
    void testTracerProperties() {
        assertThat(tracerProperties).isNotNull();
        assertThat(tracerProperties.isEnabled()).isTrue();
        assertThat(tracerProperties.isEnableWebTracing()).isTrue();
        assertThat(tracerProperties.isEnableAsyncTracing()).isTrue();
        assertThat(tracerProperties.isIncludeHeaders()).isTrue();
        assertThat(tracerProperties.isIncludeParameters()).isTrue();
        assertThat(tracerProperties.getExcludePatterns()).isEqualTo("/test/**,/health");
    }

    @Test
    void testTracerCanCreateSpan() {
        var span = tracer.spanBuilder("test-span").startSpan();
        assertThat(span).isNotNull();
        assertThat(span.getSpanContext().isValid()).isTrue();
        span.end();
    }
}

