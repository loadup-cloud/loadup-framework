package com.github.loadup.components.tracer;

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

