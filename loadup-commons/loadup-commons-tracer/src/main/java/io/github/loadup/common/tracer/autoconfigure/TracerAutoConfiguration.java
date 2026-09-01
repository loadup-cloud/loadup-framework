package io.github.loadup.common.tracer.autoconfigure;

/*-
 * #%L
 * Loadup Common Tracer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.common.tracer.TraceUtil;
import io.github.loadup.common.tracer.aspect.TracingAspect;
import io.github.loadup.common.tracer.async.AsyncTracingConfiguration;
import io.github.loadup.common.tracer.async.TracingTaskDecorator;
import io.github.loadup.common.tracer.config.TracerProperties;
import io.github.loadup.common.tracer.config.TracerProperties.ExporterConfig;
import io.github.loadup.common.tracer.filter.TracingWebFilter;
import io.github.loadup.common.tracer.provider.LoggingSpanExporterProvider;
import io.github.loadup.common.tracer.provider.NoOpSpanExporterProvider;
import io.github.loadup.common.tracer.spi.SpanExporterProvider;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Single-entry Spring Boot {@link AutoConfiguration} for the LoadUp tracer component.
 *
 * <p>Features:
 * <ul>
 *   <li>Plugin-based exporter selection via {@link ServiceLoader} SPI.</li>
 *   <li>Multi-exporter fan-out via {@link SpanExporter#composite(Iterable)}.</li>
 *   <li>Automatic fallback chain: configured exporters → logging → noop.</li>
 *   <li>Tunable {@link BatchSpanProcessor} as a memory-safe circuit-breaker.</li>
 *   <li>Conditional web filter and async context propagation.</li>
 * </ul>
 */
@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(TracerProperties.class)
@ConditionalOnProperty(prefix = "loadup.tracer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TracerAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(TracerAutoConfiguration.class);

    private final String applicationName;

    // -------------------------------------------------------------------------
    // Core OTel beans
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry openTelemetry(TracerProperties properties) {
        Resource resource = buildResource(properties);
        SpanExporter exporter = resolveExporter(properties);

        TracerProperties.Batch batch = properties.getBatch();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .setSampler(buildSampler(properties.getSamplerRatio()))
                .addSpanProcessor(BatchSpanProcessor.builder(exporter)
                        .setMaxQueueSize(batch.getMaxQueueSize())
                        .setMaxExportBatchSize(batch.getMaxExportBatchSize())
                        .setScheduleDelay(Duration.ofMillis(batch.getScheduleDelayMillis()))
                        .build())
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance())))
                .buildAndRegisterGlobal();
    }

    @Bean
    @ConditionalOnMissingBean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("loadup-tracer");
    }

    @Bean
    public TraceUtil traceUtil(OpenTelemetry openTelemetry, Tracer tracer) {
        return new TraceUtil(openTelemetry, tracer, applicationName);
    }

    // -------------------------------------------------------------------------
    // AOP: @Traced
    // -------------------------------------------------------------------------

    @Bean
    public TracingAspect tracingAspect() {
        return new TracingAspect();
    }

    // -------------------------------------------------------------------------
    // Web filter
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "loadup.tracer",
            name = "enable-web-tracing",
            havingValue = "true",
            matchIfMissing = true)
    public TracingWebFilter tracingWebFilter(TracerProperties properties, OpenTelemetry openTelemetry) {
        return new TracingWebFilter(properties, openTelemetry);
    }

    // -------------------------------------------------------------------------
    // Async context propagation
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.tracer",
            name = "enable-async-tracing",
            havingValue = "true",
            matchIfMissing = true)
    public TracingTaskDecorator tracingTaskDecorator() {
        return new TracingTaskDecorator();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "loadup.tracer",
            name = "enable-async-tracing",
            havingValue = "true",
            matchIfMissing = true)
    public static AsyncTracingConfiguration asyncTracingConfiguration(TracingTaskDecorator decorator) {
        return new AsyncTracingConfiguration(decorator);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Resource buildResource(TracerProperties properties) {
        ResourceBuilder builder =
                Resource.getDefault().toBuilder().put(AttributeKey.stringKey("service.name"), applicationName);
        properties.getAttributes().forEach((k, v) -> builder.put(AttributeKey.stringKey(k), v));
        return builder.build();
    }

    /**
     * Resolves exporters from configuration using ServiceLoader SPI.
     *
     * <p>Fallback chain:
     * <ol>
     *   <li>Each configured exporter is created; failures are logged and skipped.</li>
     *   <li>If the resulting list is empty, fall back to the built-in {@code logging} exporter.</li>
     *   <li>If even logging fails (class not on classpath), fall back to {@code noop}.</li>
     * </ol>
     */
    private SpanExporter resolveExporter(TracerProperties properties) {
        Map<String, SpanExporterProvider> providerMap = loadProviders();
        List<SpanExporter> exporters = new ArrayList<>();

        for (ExporterConfig config : properties.getExporters()) {
            SpanExporterProvider provider = providerMap.get(config.getType());
            if (provider == null) {
                log.warn(
                        "[Tracer] Unknown exporter type='{}', skipping. "
                                + "Register a SpanExporterProvider with that type via ServiceLoader.",
                        config.getType());
                continue;
            }
            try {
                exporters.add(provider.createExporter(config));
                log.info(
                        "[Tracer] Exporter type='{}' initialized (endpoint={})",
                        config.getType(),
                        config.getEndpoint());
            } catch (Exception e) {
                log.warn(
                        "[Tracer] Failed to initialize exporter type='{}', skipping. Cause: {}",
                        config.getType(),
                        e.getMessage());
            }
        }

        if (exporters.isEmpty()) {
            log.info("[Tracer] No exporters configured or all failed. Falling back to 'logging' exporter.");
            try {
                exporters.add(new LoggingSpanExporterProvider().createExporter(new ExporterConfig()));
            } catch (Exception e) {
                log.warn("[Tracer] Logging exporter unavailable, using noop fallback. Cause: {}", e.getMessage());
                exporters.add(new NoOpSpanExporterProvider().createExporter(new ExporterConfig()));
            }
        }

        return exporters.size() == 1 ? exporters.get(0) : SpanExporter.composite(exporters);
    }

    private Map<String, SpanExporterProvider> loadProviders() {
        Map<String, SpanExporterProvider> map = new HashMap<>();
        ServiceLoader.load(SpanExporterProvider.class).forEach(p -> {
            map.put(p.getType(), p);
            log.debug("[Tracer] Registered SpanExporterProvider type='{}'", p.getType());
        });
        return map;
    }

    private static Sampler buildSampler(double ratio) {
        if (ratio >= 1.0) return Sampler.alwaysOn();
        if (ratio <= 0.0) return Sampler.alwaysOff();
        return Sampler.traceIdRatioBased(ratio);
    }

    public TracerAutoConfiguration(@Value("${spring.application.name:unknown-service}") String applicationName) {
        this.applicationName = applicationName;
    }
}
