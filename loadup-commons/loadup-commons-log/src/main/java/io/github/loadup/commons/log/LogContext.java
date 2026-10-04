package io.github.loadup.commons.log;

import org.slf4j.MDC;

/** Shared MDC contract used by logging and tracing integrations. */
public final class LogContext {

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String REQUEST_ID = "requestId";
    public static final String TENANT_ID = "tenantId";
    public static final String DEFAULT_CONSOLE_PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n%wEx";
    public static final String DEFAULT_CONSOLE_PATTERN_WITHOUT_TRACE =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n%wEx";

    private LogContext() {}

    public static void clearTraceContext() {
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    public static String getSpanId() {
        return MDC.get(SPAN_ID);
    }

    public static void putRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            MDC.remove(REQUEST_ID);
        } else {
            MDC.put(REQUEST_ID, requestId);
        }
    }

    public static String getRequestId() {
        return MDC.get(REQUEST_ID);
    }

    public static void clearRequestId() {
        MDC.remove(REQUEST_ID);
    }
}
