package io.github.loadup.commons.log;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class LogContextTest {

    @AfterEach
    void tearDown() {
        LogContext.clearTraceContext();
        LogContext.clearRequestId();
    }

    @Test
    void readsTraceIdsFromMicrometerMdc() {
        MDC.put(LogContext.TRACE_ID, "0123456789abcdef0123456789abcdef");
        MDC.put(LogContext.SPAN_ID, "0123456789abcdef");

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
