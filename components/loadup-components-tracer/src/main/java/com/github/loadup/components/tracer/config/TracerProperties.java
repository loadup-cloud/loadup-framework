/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.tracer.config;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the tracer component.
 */
@Data
@Component
@ConfigurationProperties(prefix = "loadup.tracer")
public class TracerProperties {

    /**
     * Enable or disable the tracer.
     */
    private boolean enabled = true;

    /**
     * Enable web request tracing.
     */
    private boolean enableWebTracing = true;

    /**
     * Enable async task tracing.
     */
    private boolean enableAsyncTracing = true;

    /**
     * OTLP exporter endpoint (e.g., http://localhost:4317).
     */
    private String otlpEndpoint;

    /**
     * Include request headers in spans.
     */
    private boolean includeHeaders = false;

    /**
     * Include request parameters in spans.
     */
    private boolean includeParameters = false;

    /**
     * URL patterns to exclude from tracing (comma-separated).
     */
    private String excludePatterns = "/actuator/**,/health,/metrics";
}

