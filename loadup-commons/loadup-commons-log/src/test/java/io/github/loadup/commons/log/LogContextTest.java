package io.github.loadup.commons.log;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class LogContextTest {

    @AfterEach
    void tearDown() {
        LogContext.clearTraceContext();
        LogContext.clearRequestId();
    }

    @Test
    void syncTraceContextCopiesCurrentSpanIdsToMdc() {
        Span span = Span.wrap(SpanContext.create(
                "0123456789abcdef0123456789abcdef",
                "0123456789abcdef",
                TraceFlags.getSampled(),
                TraceState.getDefault()));

        try (var ignored = span.makeCurrent()) {
            LogContext.syncTraceContext();
        }

        assertThat(LogContext.getTraceId()).isEqualTo("0123456789abcdef0123456789abcdef");
        assertThat(LogContext.getSpanId()).isEqualTo("0123456789abcdef");
    }

    @Test
    void requestIdCanBeManagedIndependently() {
        LogContext.putRequestId("request-1");

        assertThat(LogContext.getRequestId()).isEqualTo("request-1");

        LogContext.clearRequestId();

        assertThat(LogContext.getRequestId()).isNull();
    }
}
