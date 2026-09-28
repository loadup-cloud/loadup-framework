package io.github.loadup.common.tracer.provider;

import io.github.loadup.common.tracer.config.TracerProperties.ExporterConfig;
import io.github.loadup.common.tracer.spi.SpanExporterProvider;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Exports spans to JUL (java.util.logging). Useful as a low-overhead fallback.
 */
public class LoggingSpanExporterProvider implements SpanExporterProvider {

    @Override
    public String getType() {
        return "logging";
    }

    @Override
    public SpanExporter createExporter(ExporterConfig config) {
        return LoggingSpanExporter.create();
    }
}
