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
    }

    public TracerProperties(boolean enabled, boolean enableWebTracing, boolean enableAsyncTracing, boolean includeHeaders, boolean includeParameters, String excludePatterns, double samplerRatio, Map<String, String> attributes, List<ExporterConfig> exporters, Batch batch, String type, String endpoint, long timeout, int maxQueueSize, int maxExportBatchSize, long scheduleDelayMillis) {
        this.enabled = enabled;
        this.enableWebTracing = enableWebTracing;
        this.enableAsyncTracing = enableAsyncTracing;
        this.includeHeaders = includeHeaders;
        this.includeParameters = includeParameters;
        this.excludePatterns = excludePatterns;
        this.samplerRatio = samplerRatio;
        this.attributes = attributes;
        this.exporters = exporters;
        this.batch = batch;
        this.type = type;
        this.endpoint = endpoint;
        this.timeout = timeout;
        this.maxQueueSize = maxQueueSize;
        this.maxExportBatchSize = maxExportBatchSize;
        this.scheduleDelayMillis = scheduleDelayMillis;
    }

    public TracerProperties() {
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isEnableWebTracing() {
        return this.enableWebTracing;
    }

    public boolean isEnableAsyncTracing() {
        return this.enableAsyncTracing;
    }

    public boolean isIncludeHeaders() {
        return this.includeHeaders;
    }

    public boolean isIncludeParameters() {
        return this.includeParameters;
    }

    public String getExcludePatterns() {
        return this.excludePatterns;
    }

    public double getSamplerRatio() {
        return this.samplerRatio;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public List<ExporterConfig> getExporters() {
        return this.exporters;
    }

    public Batch getBatch() {
        return this.batch;
    }

    public String getType() {
        return this.type;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public int getMaxQueueSize() {
        return this.maxQueueSize;
    }

    public int getMaxExportBatchSize() {
        return this.maxExportBatchSize;
    }

    public long getScheduleDelayMillis() {
        return this.scheduleDelayMillis;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEnableWebTracing(boolean enableWebTracing) {
        this.enableWebTracing = enableWebTracing;
    }

    public void setEnableAsyncTracing(boolean enableAsyncTracing) {
        this.enableAsyncTracing = enableAsyncTracing;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    public void setIncludeParameters(boolean includeParameters) {
        this.includeParameters = includeParameters;
    }

    public void setExcludePatterns(String excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public void setSamplerRatio(double samplerRatio) {
        this.samplerRatio = samplerRatio;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setExporters(List<ExporterConfig> exporters) {
        this.exporters = exporters;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public void setMaxExportBatchSize(int maxExportBatchSize) {
        this.maxExportBatchSize = maxExportBatchSize;
    }

    public void setScheduleDelayMillis(long scheduleDelayMillis) {
        this.scheduleDelayMillis = scheduleDelayMillis;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(enabled, enableWebTracing, enableAsyncTracing, includeHeaders, includeParameters, excludePatterns, samplerRatio, attributes, exporters, batch, type, endpoint, timeout, maxQueueSize, maxExportBatchSize, scheduleDelayMillis);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TracerProperties other = (TracerProperties) o;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(enableWebTracing, other.enableWebTracing)) return false;
        if (!java.util.Objects.equals(enableAsyncTracing, other.enableAsyncTracing)) return false;
        if (!java.util.Objects.equals(includeHeaders, other.includeHeaders)) return false;
        if (!java.util.Objects.equals(includeParameters, other.includeParameters)) return false;
        if (!java.util.Objects.equals(excludePatterns, other.excludePatterns)) return false;
        if (!java.util.Objects.equals(samplerRatio, other.samplerRatio)) return false;
        if (!java.util.Objects.equals(attributes, other.attributes)) return false;
        if (!java.util.Objects.equals(exporters, other.exporters)) return false;
        if (!java.util.Objects.equals(batch, other.batch)) return false;
        if (!java.util.Objects.equals(type, other.type)) return false;
        if (!java.util.Objects.equals(endpoint, other.endpoint)) return false;
        if (!java.util.Objects.equals(timeout, other.timeout)) return false;
        if (!java.util.Objects.equals(maxQueueSize, other.maxQueueSize)) return false;
        if (!java.util.Objects.equals(maxExportBatchSize, other.maxExportBatchSize)) return false;
        if (!java.util.Objects.equals(scheduleDelayMillis, other.scheduleDelayMillis)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "TracerProperties(" + "enabled=" + enabled + ", " + "enableWebTracing=" + enableWebTracing + ", " + "enableAsyncTracing=" + enableAsyncTracing + ", " + "includeHeaders=" + includeHeaders + ", " + "includeParameters=" + includeParameters + ", " + "excludePatterns=" + excludePatterns + ", " + "samplerRatio=" + samplerRatio + ", " + "attributes=" + attributes + ", " + "exporters=" + exporters + ", " + "batch=" + batch + ", " + "type=" + type + ", " + "endpoint=" + endpoint + ", " + "timeout=" + timeout + ", " + "maxQueueSize=" + maxQueueSize + ", " + "maxExportBatchSize=" + maxExportBatchSize + ", " + "scheduleDelayMillis=" + scheduleDelayMillis + ")";
    }
}
