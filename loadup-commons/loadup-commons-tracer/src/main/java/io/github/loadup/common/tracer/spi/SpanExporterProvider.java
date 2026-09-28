package io.github.loadup.common.tracer.spi;

import io.github.loadup.common.tracer.config.TracerProperties.ExporterConfig;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * SPI for plugging in custom OpenTelemetry {@link SpanExporter} implementations.
 *
 * <p>Built-in types: {@code logging}, {@code otlp}, {@code noop}.
 * Additional types can be registered by placing a
 * {@code META-INF/services/io.github.loadup.common.tracer.spi.SpanExporterProvider}
 * file in any JAR on the classpath.
 */
public interface SpanExporterProvider {

    /**
     * Unique type identifier referenced in {@code loadup.tracer.exporters[].type}.
     *
     * @return the type key (lower-case, e.g. "otlp")
     */
    String getType();

    /**
     * Creates and returns a configured {@link SpanExporter}.
     *
     * @param config the per-exporter configuration (endpoint, timeout, …)
     * @return the initialized exporter
     */
    SpanExporter createExporter(ExporterConfig config);
}
