package io.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-api
 * %%
 * Copyright (C) 2025 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.slf4j.MDC;

/**
 * @author Lise
 */
public class MDCUtils {

    public static final String MDC_TRACE_ID = "TraceId";

    public static final String MDC_SPAN_ID = "SpanId";

    public static final String MDC_TENANT_ID = "TenantId";

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
