package io.github.loadup.components.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ObservabilityIntegrationTest {
    @Test
    void copiesCurrentTraceIdWithoutCreatingAnotherSpan() throws Exception {
        Tracer tracer = mock(Tracer.class);
        Span span = mock(Span.class);
        TraceContext context = mock(TraceContext.class);
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(context);
        when(context.traceId()).thenReturn("0123456789abcdef0123456789abcdef");

        MockHttpServletResponse response = new MockHttpServletResponse();
        new TraceResponseHeaderFilter(tracer)
                .doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertThat(response.getHeader("traceId")).isEqualTo("0123456789abcdef0123456789abcdef");
        verify(tracer).currentSpan();
        verifyNoMoreInteractions(tracer);
    }

    @Test
    void omitsTraceHeaderWhenNoSpanIsActive() throws Exception {
        Tracer tracer = mock(Tracer.class);
        MockHttpServletResponse response = new MockHttpServletResponse();

        new TraceResponseHeaderFilter(tracer)
                .doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertThat(response.getHeader("traceId")).isNull();
    }

    @Test
    void countsEnvelopeOutcomesWithoutHighCardinalityTags() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ApiResultMetrics metrics = new ApiResultMetrics(registry);
        metrics.record(true);
        metrics.record(false);
        metrics.record(false);

        assertThat(registry.get("loadup.api.responses").tag("outcome", "success").counter().count())
                .isEqualTo(1.0);
        assertThat(registry.get("loadup.api.responses").tag("outcome", "failure").counter().count())
                .isEqualTo(2.0);
    }
}
