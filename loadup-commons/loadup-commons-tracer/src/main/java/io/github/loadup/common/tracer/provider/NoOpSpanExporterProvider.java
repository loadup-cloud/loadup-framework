package io.github.loadup.common.tracer.provider;

import io.github.loadup.common.tracer.config.TracerProperties.ExporterConfig;
import io.github.loadup.common.tracer.spi.SpanExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;

/**
 * No-op exporter that silently discards all spans.
 * Used as the ultimate fallback when all other exporters fail.
 */
public class NoOpSpanExporterProvider implements SpanExporterProvider {

    @Override
    public String getType() {
        return "noop";
    }

    @Override
    public SpanExporter createExporter(ExporterConfig config) {
        return new SpanExporter() {
            @Override
            public CompletableResultCode export(Collection<SpanData> spans) {
                return CompletableResultCode.ofSuccess();
            }

            @Override
            public CompletableResultCode flush() {
                return CompletableResultCode.ofSuccess();
            }

            @Override
            public CompletableResultCode shutdown() {
                return CompletableResultCode.ofSuccess();
            }
        };
    }
}
