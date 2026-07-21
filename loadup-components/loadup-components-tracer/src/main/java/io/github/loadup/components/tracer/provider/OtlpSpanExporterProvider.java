package io.github.loadup.components.tracer.provider;

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

import io.github.loadup.components.tracer.config.TracerProperties.ExporterConfig;
import io.github.loadup.components.tracer.spi.SpanExporterProvider;
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
