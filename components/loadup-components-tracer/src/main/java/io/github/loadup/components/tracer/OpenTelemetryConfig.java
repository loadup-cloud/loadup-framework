package io.github.loadup.components.tracer;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import io.github.loadup.components.tracer.config.TracerProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "loadup.tracer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenTelemetryConfig {

    @Value("${spring.application.name:unknown-service}")
    private String applicationName;

    private final TracerProperties tracerProperties;

    @Bean
    public OpenTelemetry openTelemetry() {
        // Create resource with service name and custom attributes
        var resourceBuilder = Resource.getDefault().toBuilder()
                .put(AttributeKey.stringKey("service.name"), applicationName)
                .put(AttributeKey.stringKey("service.version"), "1.0.0");

        // Add custom resource attributes
        tracerProperties
                .getResource()
                .getAttributes()
                .forEach((key, value) -> resourceBuilder.put(AttributeKey.stringKey(key), value));
        Resource resource = resourceBuilder.build();

        // Build span exporters from configuration
        List<SpanExporter> spanExporters = buildSpanExporters();
        SpanExporter compositeExporter =
                spanExporters.isEmpty() ? LoggingSpanExporter.create() : SpanExporter.composite(spanExporters);

        // Create tracer provider with sampler and batch processor
        var batchConfig = tracerProperties.getBatchProcessor();
        var tracerBuilder = SdkTracerProvider.builder()
                .setResource(resource)
                .setSampler(buildSampler())
                .addSpanProcessor(BatchSpanProcessor.builder(compositeExporter)
                        .setMaxQueueSize(batchConfig.getMaxQueueSize())
                        .setMaxExportBatchSize(batchConfig.getMaxExportBatchSize())
                        .setScheduleDelay(Duration.ofMillis(batchConfig.getScheduleDelayMillis()))
                        .setExporterTimeout(Duration.ofMillis(batchConfig.getExportTimeoutMillis()))
                        .build());

        SdkTracerProvider sdkTracerProvider = tracerBuilder.build();

        // Meter and Logger providers
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(PeriodicMetricReader.builder(LoggingMetricExporter.create())
                        .build())
                .build();

        SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
                .setResource(resource)
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(SystemOutLogRecordExporter.create())
                        .build())
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setMeterProvider(sdkMeterProvider)
                .setLoggerProvider(sdkLoggerProvider)
                .setPropagators(ContextPropagators.create(TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance())))
                .build();
    }

    /**
     * Build span exporters based on configuration.
     */
    private List<SpanExporter> buildSpanExporters() {
        List<SpanExporter> exporters = new ArrayList<>();

        for (TracerProperties.ExporterConfig config : tracerProperties.getExporters()) {
            try {
                SpanExporter exporter = createExporter(config);
                if (exporter != null) {
                    exporters.add(exporter);
                }
            } catch (Exception e) {
                // Log error but continue with other exporters
                System.err.println("Failed to create exporter: " + config.getType() + ", error: " + e.getMessage());
            }
        }

        // If no exporters configured, add logging exporter for development
        if (exporters.isEmpty()) {
            exporters.add(LoggingSpanExporter.create());
        }

        return exporters;
    }

    /**
     * Create span exporter based on type.
     */
    private SpanExporter createExporter(TracerProperties.ExporterConfig config) {
        return switch (config.getType()) {
            case OTLP -> createOtlpExporter(config);
            case SKYWALKING -> createSkyWalkingExporter(config);
            case ZIPKIN -> createZipkinExporter(config);
            case LOGGING -> LoggingSpanExporter.create();
        };
    }

    /**
     * Create OTLP exporter (gRPC or HTTP).
     */
    private SpanExporter createOtlpExporter(TracerProperties.ExporterConfig config) {
        if (!StringUtils.hasText(config.getEndpoint())) {
            throw new IllegalArgumentException("OTLP endpoint is required");
        }

        var builder = OtlpGrpcSpanExporter.builder()
                .setEndpoint(config.getEndpoint())
                .setTimeout(Duration.ofSeconds(config.getTimeout()));

        // Add custom headers
        if (config.getHeaders() != null && !config.getHeaders().isEmpty()) {
            config.getHeaders().forEach((key, value) -> builder.addHeader(key, value));
        }

        return builder.build();
    }

    /**
     * Create SkyWalking exporter.
     * Note: SkyWalking 9.x+ supports OTLP protocol.
     */
    private SpanExporter createSkyWalkingExporter(TracerProperties.ExporterConfig config) {
        String oapServer = StringUtils.hasText(config.getOapServer()) ? config.getOapServer() : config.getEndpoint();

        if (!StringUtils.hasText(oapServer)) {
            throw new IllegalArgumentException("SkyWalking OAP server endpoint is required");
        }

        // Use OTLP exporter pointing to SkyWalking OAP receiver
        var builder = OtlpGrpcSpanExporter.builder()
                .setEndpoint(oapServer)
                .setTimeout(Duration.ofSeconds(config.getTimeout()));

        // Add authentication header if provided
        if (StringUtils.hasText(config.getAuthentication())) {
            builder.addHeader("Authentication", config.getAuthentication());
        }

        return builder.build();
    }

    /**
     * Create Zipkin exporter.
     * TODO: Add actual Zipkin exporter when opentelemetry-exporter-zipkin dependency is added.
     */
    private SpanExporter createZipkinExporter(TracerProperties.ExporterConfig config) {
        if (!StringUtils.hasText(config.getEndpoint())) {
            throw new IllegalArgumentException("Zipkin endpoint is required");
        }

        // For now, use OTLP as placeholder
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(config.getEndpoint())
                .setTimeout(Duration.ofSeconds(config.getTimeout()))
                .build();
    }

    /**
     * Build sampler based on configuration.
     */
    private Sampler buildSampler() {
        var samplerConfig = tracerProperties.getSampler();

        return switch (samplerConfig.getType()) {
            case ALWAYS_ON -> Sampler.alwaysOn();
            case ALWAYS_OFF -> Sampler.alwaysOff();
            case PROBABILISTIC -> Sampler.traceIdRatioBased(samplerConfig.getProbability());
            case PARENT_BASED -> Sampler.parentBased(Sampler.traceIdRatioBased(samplerConfig.getProbability()));
        };
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(applicationName);
    }

    @Bean
    public Meter meter(OpenTelemetry openTelemetry) {
        return openTelemetry.getMeter(applicationName);
    }

    /**
     * Integrate Micrometer MeterRegistry with OpenTelemetry if available.
     */
    @Bean
    public MeterRegistryIntegration meterRegistryIntegration(
            @Autowired(required = false) MeterRegistry meterRegistry, OpenTelemetry openTelemetry) {
        if (meterRegistry != null) {
            return new MeterRegistryIntegration(meterRegistry);
        }
        return new MeterRegistryIntegration(null);
    }

    @Bean
    public ContextPropagators contextPropagators() {
        return ContextPropagators.create(new CustomTextMapPropagator());
    }

    @Bean
    public TextMapPropagator textMapPropagator(ContextPropagators propagators) {
        return propagators.getTextMapPropagator();
    }

    /**
     * Custom TextMapPropagator that supports both W3C TraceContext and custom traceId header.
     */
    static class CustomTextMapPropagator implements TextMapPropagator {

        private static final String CUSTOM_TRACE_HEADER = "traceId";
        private final W3CTraceContextPropagator w3cPropagator = W3CTraceContextPropagator.getInstance();

        @Override
        @SuppressWarnings("unchecked")
        public void inject(
                io.opentelemetry.context.Context context,
                Object carrier,
                io.opentelemetry.context.propagation.TextMapSetter setter) {
            SpanContext spanContext = Span.fromContext(context).getSpanContext();
            if (spanContext.isValid()) {
                setter.set(carrier, CUSTOM_TRACE_HEADER, spanContext.getTraceId());
                w3cPropagator.inject(context, carrier, setter);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public io.opentelemetry.context.Context extract(
                io.opentelemetry.context.Context context,
                Object carrier,
                io.opentelemetry.context.propagation.TextMapGetter getter) {
            io.opentelemetry.context.Context w3cContext = w3cPropagator.extract(context, carrier, getter);
            if (w3cContext != context) {
                return w3cContext;
            }

            String traceId = getter.get(carrier, CUSTOM_TRACE_HEADER);
            if (traceId != null && traceId.length() == 32) {
                SpanContext spanContext = SpanContext.create(
                        traceId, "0000000000000000", TraceFlags.getSampled(), TraceState.getDefault());
                return context.with(Span.wrap(spanContext));
            }
            return context;
        }

        @Override
        public List<String> fields() {
            return Arrays.asList("traceparent", CUSTOM_TRACE_HEADER);
        }
    }

    /**
     * Integration class for MeterRegistry with OpenTelemetry
     */
    static class MeterRegistryIntegration {
        private final MeterRegistry meterRegistry;

        MeterRegistryIntegration(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        public MeterRegistry getMeterRegistry() {
            return meterRegistry;
        }
    }
}
