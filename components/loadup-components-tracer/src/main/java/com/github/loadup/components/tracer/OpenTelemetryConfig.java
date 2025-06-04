/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.tracer;

/*-
 * #%L
 * loadup-components-tracer
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

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {

    @Value("${spring.application.name:''}")
    private String applicationName;

    @Value("${otel.exporter.otlp.endpoint:''}")
    private String otelEndpoint;

    @Bean
    public OpenTelemetry openTelemetry() {
        LoggingSpanExporter logExporter = LoggingSpanExporter.create();
        SpanExporter spanExporter = SpanExporter.composite(logExporter);

        // if (StringUtils.hasText(otelEndpoint)) {
        // OtlpGrpcSpanExporter otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
        //        .setEndpoint(otelEndpoint)
        //        .build();
        // spanExporter = SpanExporter.composite(logExporter, otlpGrpcSpanExporter);
        // }

        // Resource resource = Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME,
        // applicationName).put(
        //        ResourceAttributes.SERVICE_VERSION, "1.0.0").build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
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

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setMeterProvider(sdkMeterProvider)
                .setLoggerProvider(sdkLoggerProvider)
                .setPropagators(ContextPropagators.create(TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance())))
                .build();

        return openTelemetry;
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(applicationName);
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
}
