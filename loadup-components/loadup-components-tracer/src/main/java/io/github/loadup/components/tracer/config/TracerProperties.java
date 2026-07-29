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
import org.springframework.boot.context.properties.ConfigurationProperties;

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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }

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

        public int getMaxQueueSize() {
            return maxQueueSize;
        }

        public void setMaxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
        }

        public int getMaxExportBatchSize() {
            return maxExportBatchSize;
        }

        public void setMaxExportBatchSize(int maxExportBatchSize) {
            this.maxExportBatchSize = maxExportBatchSize;
        }

        public long getScheduleDelayMillis() {
            return scheduleDelayMillis;
        }

        public void setScheduleDelayMillis(long scheduleDelayMillis) {
            this.scheduleDelayMillis = scheduleDelayMillis;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnableWebTracing() {
        return enableWebTracing;
    }

    public void setEnableWebTracing(boolean enableWebTracing) {
        this.enableWebTracing = enableWebTracing;
    }

    public boolean isEnableAsyncTracing() {
        return enableAsyncTracing;
    }

    public void setEnableAsyncTracing(boolean enableAsyncTracing) {
        this.enableAsyncTracing = enableAsyncTracing;
    }

    public boolean isIncludeHeaders() {
        return includeHeaders;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    public boolean isIncludeParameters() {
        return includeParameters;
    }

    public void setIncludeParameters(boolean includeParameters) {
        this.includeParameters = includeParameters;
    }

    public String getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(String excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public double getSamplerRatio() {
        return samplerRatio;
    }

    public void setSamplerRatio(double samplerRatio) {
        this.samplerRatio = samplerRatio;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public List<ExporterConfig> getExporters() {
        return exporters;
    }

    public void setExporters(List<ExporterConfig> exporters) {
        this.exporters = exporters;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }
}
