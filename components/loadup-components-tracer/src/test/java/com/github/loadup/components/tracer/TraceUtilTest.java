package com.github.loadup.components.tracer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestConfiguration.class)
@TestPropertySource(properties = {
    "spring.application.name=test-service",
    "loadup.tracer.enabled=true"
})
class TraceUtilTest {

    @Autowired
    private Tracer tracer;

    @BeforeEach
    void setUp() {
        // Clean up any existing trace context before each test
        TraceUtil.getTraceContext().clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up trace context after each test to avoid interference
        TraceUtil.getTraceContext().clear();
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
        // Get the trace context and ensure it's clean
        TraceContext context = TraceUtil.getTraceContext();
        assertThat(context).isNotNull();

        // Verify context is empty at the start (should be cleaned by setUp)
        assertThat(context.isEmpty()).isTrue();
        assertThat(context.getThreadLocalSpanSize()).isEqualTo(0);

        // Create a span and verify it's stored in context
        Span span = TraceUtil.createSpan("context-test");
        assertThat(span).isNotNull();
        assertThat(context.isEmpty()).isFalse();
        assertThat(context.getThreadLocalSpanSize()).isEqualTo(1);

        // Retrieve and verify the span from context
        Span retrievedSpan = context.getCurrentSpan();
        assertThat(retrievedSpan).isNotNull();
        assertThat(retrievedSpan).isEqualTo(span);

        // End span before clearing context
        span.end();

        // Clear context and verify it's empty
        context.clear();
        assertThat(context.isEmpty()).isTrue();
        assertThat(context.getThreadLocalSpanSize()).isEqualTo(0);
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



