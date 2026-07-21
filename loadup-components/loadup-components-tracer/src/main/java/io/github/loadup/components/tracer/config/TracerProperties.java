package io.github.loadup.components.tracer.config;

/*-
 * #%L
 * Loadup Components Tracer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "loadup.tracer")
public class TracerProperties {

    /**
     * Whether the tracer is enabled.
     */
    private boolean enabled = true;

    /**
     * Whether HTTP request tracing via filter is enabled.
     */
    private boolean enableWebTracing = true;

    /**
     * Whether async task trace context propagation is enabled.
     */
    private boolean enableAsyncTracing = true;

    /**
     * Whether to include HTTP request headers as span attributes.
     */
    private boolean includeHeaders = false;

    /**
     * Whether to include HTTP request parameters as span attributes.
     */
    private boolean includeParameters = false;

    /**
     * Comma-separated URL patterns to exclude from tracing.
     */
    private String excludePatterns = "/actuator/**,/health,/metrics";

    /**
     * Sampling ratio: 1.0 = always on, 0.0 = always off, value between = ratio-based.
     * Applies to the root sampler.
     */
    private double samplerRatio = 1.0;

    /**
     * Custom resource attributes injected into every span (e.g. environment, region).
     */
    private Map<String, String> attributes = new HashMap<>();

    /**
     * List of exporters to enable. Multiple exporters are fan-out via composite.
     * If empty, falls back to the logging exporter automatically.
     */
    private List<ExporterConfig> exporters = new ArrayList<>();

    /**
     * BatchSpanProcessor tuning – acts as the primary circuit-breaker / fallback.
     */
    private Batch batch = new Batch();

    @Data
    public static class ExporterConfig {

        /**
         * Exporter type: otlp | zipkin | logging | noop.
         * Additional types can be registered via ServiceLoader SPI.
         */
        private String type = "logging";

        /**
         * Exporter endpoint.
         * otlp:   e.g. http://localhost:4317
         * zipkin: e.g. http://localhost:9411/api/v2/spans
         */
        private String endpoint;

        /**
         * Exporter connection / export timeout in milliseconds.
         */
        private long timeout = 10000;
    }

    @Data
    public static class Batch {

        /**
         * Maximum number of spans queued in the ring buffer before new ones are dropped.
         * This acts as a memory-safe circuit-breaker when the backend is unavailable.
         */
        private int maxQueueSize = 2048;

        /**
         * Maximum number of spans exported in one batch.
         */
        private int maxExportBatchSize = 512;

        /**
         * Delay between two consecutive export calls in milliseconds.
         */
        private long scheduleDelayMillis = 5000;
    }
}
