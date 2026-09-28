package io.github.loadup.common.tracer.provider;

import io.github.loadup.common.tracer.config.TracerProperties.ExporterConfig;
import io.github.loadup.common.tracer.spi.SpanExporterProvider;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;

/**
 * Exports spans via OTLP/HTTP (e.g. to an OpenTelemetry Collector or Jaeger).
 *
 * <p>Default endpoint: {@code http://localhost:4318/v1/traces}.
 */
public class OtlpSpanExporterProvider implements SpanExporterProvider {

    @Override
    public String getType() {
        return "otlp";
    }

    @Override
    public SpanExporter createExporter(ExporterConfig config) {
        OtlpHttpSpanExporterBuilder builder =
                OtlpHttpSpanExporter.builder().setTimeout(Duration.ofMillis(config.getTimeout()));
        if (config.getEndpoint() != null && !config.getEndpoint().isBlank()) {
            builder.setEndpoint(config.getEndpoint());
        }
        return builder.build();
    }
}
