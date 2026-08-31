package io.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-api
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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
