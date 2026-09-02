/*-
 * #%L
 * Loadup Common Log
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
package io.github.loadup.commons.log;

import io.opentelemetry.api.trace.Span;
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

    public static void putTraceContext(Span span) {
        if (span != null && span.getSpanContext().isValid()) {
            MDC.put(TRACE_ID, span.getSpanContext().getTraceId());
            MDC.put(SPAN_ID, span.getSpanContext().getSpanId());
        } else {
            clearTraceContext();
        }
    }

    public static void syncTraceContext() {
        putTraceContext(Span.current());
    }

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
