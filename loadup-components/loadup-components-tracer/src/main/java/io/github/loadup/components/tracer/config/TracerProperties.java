package io.github.loadup.components.tracer.config;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configuration properties for the tracer component. */
@Data
@Validated
@ConfigurationProperties(prefix = "loadup.tracer")
public class TracerProperties {

    /** Enable or disable the tracer. */
    private boolean enabled = true;

    /** Enable web request tracing. */
    private boolean enableWebTracing = true;

    /** Enable async task tracing. */
    private boolean enableAsyncTracing = true;


    /** Include request headers in spans. */
    private boolean includeHeaders = false;

    /** Include request parameters in spans. */
    private boolean includeParameters = false;

    /** URL patterns to exclude from tracing (comma-separated). */
    private String excludePatterns = "/actuator/**,/health,/metrics";

    /** Exporter configurations */
    private List<ExporterConfig> exporters = new ArrayList<>();

    /** Sampler configuration */
    private SamplerConfig sampler = new SamplerConfig();

    /** Batch processor configuration */
    private BatchProcessorConfig batchProcessor = new BatchProcessorConfig();

    /** Resource attributes */
    private ResourceConfig resource = new ResourceConfig();

    /**
     * Exporter configuration
     */
    @Data
    public static class ExporterConfig {
        /** Exporter type: otlp, skywalking, zipkin, logging */
        private ExporterType type = ExporterType.OTLP;

        /** Endpoint URL */
        private String endpoint;

        /** Protocol (for OTLP): grpc or http */
        private String protocol = "grpc";

        /** Timeout in seconds */
        private int timeout = 10;

        /** Compression: none, gzip */
        private String compression = "none";

        /** Custom headers */
        private Map<String, String> headers = new HashMap<>();

        /** SkyWalking specific: OAP server endpoint */
        private String oapServer;

        /** SkyWalking specific: authentication token */
        private String authentication;
    }

    /**
     * Exporter types
     */
    public enum ExporterType {
        OTLP,
        SKYWALKING,
        ZIPKIN,
        LOGGING
    }

    /**
     * Sampler configuration
     */
    @Data
    public static class SamplerConfig {
        /** Sampler type: always_on, always_off, probabilistic, parent_based */
        private SamplerType type = SamplerType.PARENT_BASED;

        /** Sampling probability (0.0 to 1.0) for probabilistic sampler */
        @Min(0)
        @Max(1)
        private double probability = 1.0;
    }

    /**
     * Sampler types
     */
    public enum SamplerType {
        ALWAYS_ON,
        ALWAYS_OFF,
        PROBABILISTIC,
        PARENT_BASED
    }

    /**
     * Batch processor configuration
     */
    @Data
    public static class BatchProcessorConfig {
        /** Maximum queue size */
        private int maxQueueSize = 2048;

        /** Maximum export batch size */
        private int maxExportBatchSize = 512;

        /** Schedule delay in milliseconds */
        private long scheduleDelayMillis = 5000;

        /** Export timeout in milliseconds */
        private long exportTimeoutMillis = 30000;
    }

    /**
     * Resource configuration
     */
    @Data
    public static class ResourceConfig {
        /** Custom resource attributes */
        private Map<String, String> attributes = new HashMap<>();
    }
}
