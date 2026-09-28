package io.github.loadup.commons.util;

import io.github.loadup.commons.log.LogContext;
import org.slf4j.MDC;

/**
 * @author Lise
 */
public class MDCUtils {

    public static final String MDC_TRACE_ID = LogContext.TRACE_ID;

    public static final String MDC_SPAN_ID = LogContext.SPAN_ID;

    public static final String MDC_TENANT_ID = LogContext.TENANT_ID;

    public static void logStoppedSpan() {
        MDC.remove(MDC_TRACE_ID);
        MDC.remove(MDC_SPAN_ID);
        //        Span span = TracerUtils.getSpan();
        //        if (span != null) {
        //            MDC.put(MDC_TRACE_ID, TracerUtils.getTracerId());
        //        }
    }

    public static void logTenantId(String tenantId) {
        MDC.put(MDC_TENANT_ID, tenantId);
    }

    public static void clearTenantId() {
        MDC.remove(MDC_TENANT_ID);
    }
}
