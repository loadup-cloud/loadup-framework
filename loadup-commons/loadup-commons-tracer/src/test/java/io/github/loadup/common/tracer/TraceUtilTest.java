package io.github.loadup.common.tracer;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TestConfiguration.class)
@TestPropertySource(properties = {"spring.application.name=test-service", "loadup.tracer.enabled=true"})
class TraceUtilTest {

    @Autowired
    private Tracer tracer;

    @BeforeEach
    void setUp() {
        // Clean up any existing trace context before each test
        TraceUtil.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Clean up trace context after each test to avoid interference
        TraceUtil.clearContext();
    }

    @Test
    void testGetTracer() {
        Tracer tracer = TraceUtil.getTracer();
        assertThat(tracer).isNotNull();
    }

    @Test
    void testGetApplicationName() {
        String appName = TraceUtil.getApplicationName();
        assertThat(appName).isEqualTo("test-service");
    }

    @Test
    void testCreateSpan() {
        Span span = TraceUtil.createSpan("test-operation");
        assertThat(span).isNotNull();
        assertThat(span.getSpanContext().isValid()).isTrue();

        // Verify span is stored in context
        Span currentSpan = TraceUtil.getSpan();
        assertThat(currentSpan).isEqualTo(span);

        span.end();
    }

    @Test
    void testGetTracerId() {
        Span span = TraceUtil.createSpan("test-trace-id");
        try {
            String traceId = TraceUtil.getTracerId();
            assertThat(traceId).isNotNull();
            assertThat(traceId).hasSize(32); // TraceId should be 32 hex characters
        } finally {
            span.end();
        }
    }

    @Test
    void testTraceContext() {
        // Verify context is empty at the start (should be cleaned by setUp)
        assertThat(TraceUtil.getSpan()).isNull();

        // Create a span and verify it's stored in context
        Span span = TraceUtil.createSpan("context-test");
        assertThat(span).isNotNull();
        assertThat(TraceUtil.getSpan()).isEqualTo(span);

        // Retrieve and verify the span from context
        assertThat(TraceUtil.getSpan()).isEqualTo(span);

        // End span before clearing context
        span.end();

        // Clear context and verify it's empty
        TraceUtil.clearContext();
        assertThat(TraceUtil.getSpan()).isNull();
    }

    @Test
    void testLogTraceId() {
        Span span = TraceUtil.createSpan("log-test");
        try {
            // Should not throw exception
            TraceUtil.logTraceId(span);
            TraceUtil.logTraceId("custom-trace-id");
            TraceUtil.clearTraceId();
        } finally {
            span.end();
        }
    }
}
