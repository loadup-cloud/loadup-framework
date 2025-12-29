package com.github.loadup.components.tracer;

import io.opentelemetry.api.trace.Span;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for TraceContext.
 */
class TraceContextTest {

    @Test
    void testPushAndPop() {
        TraceContext context = new TraceContext();
        assertThat(context.isEmpty()).isTrue();

        Span span = Span.getInvalid();
        context.push(span);

        assertThat(context.isEmpty()).isFalse();
        assertThat(context.getThreadLocalSpanSize()).isEqualTo(1);

        Span retrieved = context.pop();
        assertThat(retrieved).isEqualTo(span);
        assertThat(context.isEmpty()).isTrue();
    }

    @Test
    void testGetCurrentSpan() {
        TraceContext context = new TraceContext();

        Span nullSpan = context.getCurrentSpan();
        assertThat(nullSpan).isNull();

        Span span = Span.getInvalid();
        context.push(span);

        Span current = context.getCurrentSpan();
        assertThat(current).isEqualTo(span);
    }

    @Test
    void testClear() {
        TraceContext context = new TraceContext();
        Span span = Span.getInvalid();

        context.push(span);
        assertThat(context.isEmpty()).isFalse();

        context.clear();
        assertThat(context.isEmpty()).isTrue();
    }

    @Test
    void testPushNull() {
        TraceContext context = new TraceContext();
        context.push(null);

        // Pushing null should not change state
        assertThat(context.isEmpty()).isTrue();
    }

    @Test
    void testPopEmpty() {
        TraceContext context = new TraceContext();
        Span span = context.pop();

        assertThat(span).isNull();
    }
}

