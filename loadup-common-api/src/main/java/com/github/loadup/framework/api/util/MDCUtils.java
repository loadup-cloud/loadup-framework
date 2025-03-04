package com.github.loadup.framework.api.util;

/*-
 * #%L
 * loadup-api
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.opentelemetry.api.trace.Span;
import org.slf4j.MDC;

/**
 * @author Laysan
 */
public class MDCUtils {

    public static final String MDC_TRACE_ID = "TraceId";

    public static final String MDC_SPAN_ID = "SpanId";

    public static final String MDC_TENANT_ID = "TenantId";

    public static void logStartedSpan(Span currentSpan) {
        if (currentSpan != null) {
//            MDC.put(MDC_TRACE_ID, TracerUtils.getTracerId());
        }
    }

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
