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
import io.opentelemetry.exporter.logging.*;
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
        LoggingSpanExporter logExporter = LoggingSpanExporter.create();
        SpanExporter spanExporter = logExporter;

        // Configure OTLP exporter if endpoint is provided
        if (StringUtils.hasText(tracerProperties.getOtlpEndpoint())) {
            OtlpGrpcSpanExporter otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
                    .setEndpoint(tracerProperties.getOtlpEndpoint())
                    .build();
            spanExporter = SpanExporter.composite(logExporter, otlpGrpcSpanExporter);
        }

        // Create resource with service name and version
        Resource resource = Resource.getDefault().toBuilder()
                .put(AttributeKey.stringKey("service.name"), applicationName)
                .put(AttributeKey.stringKey("service.version"), "1.0.0")
                .build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .build();

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.builder(LoggingMetricExporter.create())
                        .build())
                .build();

        SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
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
     *
     * @param meterRegistry Optional MeterRegistry from Micrometer
     * @param openTelemetry OpenTelemetry instance
     */
    @Bean
    public MeterRegistryIntegration meterRegistryIntegration(
            @Autowired(required = false) MeterRegistry meterRegistry, OpenTelemetry openTelemetry) {
        if (meterRegistry != null) {
            // MeterRegistry is available, metrics will be collected through Micrometer
            // OpenTelemetry metrics can coexist with Micrometer
            return new MeterRegistryIntegration(meterRegistry);
        }
        return new MeterRegistryIntegration(null);
    }

    @Bean
    public ContextPropagators contextPropagators() {
        // return ContextPropagators.create(W3CTraceContextPropagator.getInstance());
        return ContextPropagators.create(new CustomTextMapPropagator());
    }

    @Bean
    public TextMapPropagator textMapPropagator(ContextPropagators propagators) {
        return propagators.getTextMapPropagator();
    }

    static class CustomTextMapPropagator implements TextMapPropagator {

        private static final String CUSTOM_TRACE_HEADER = "traceId";
        private final W3CTraceContextPropagator w3cPropagator = W3CTraceContextPropagator.getInstance();

        @Override
        @SuppressWarnings("unchecked")
        public void inject(
                io.opentelemetry.context.Context context,
                Object carrier,
                io.opentelemetry.context.propagation.TextMapSetter setter) {
            // w3cPropagator.inject(context, carrier, setter);
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
            // return w3cPropagator.extract(context, carrier, getter);
            io.opentelemetry.context.Context w3cContext = w3cPropagator.extract(context, carrier, getter);
            if (w3cContext != context) {
                return w3cContext;
            }

            String traceId = getter.get(carrier, CUSTOM_TRACE_HEADER);
            if (traceId != null && traceId.length() == 32) {
                SpanContext spanContext = SpanContext.create(
                        traceId,
                        "0000000000000000", // Dummy spanId, 不使用
                        TraceFlags.getSampled(),
                        TraceState.getDefault());
                return context.with(Span.wrap(spanContext));
            }
            return context; // 如果没有 traceId 或不合法, 则返回 context
        }

        @Override
        public List<String> fields() {
            return Arrays.asList("traceparent", CUSTOM_TRACE_HEADER);
        }
    }

    /** Integration class for MeterRegistry with OpenTelemetry */
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
