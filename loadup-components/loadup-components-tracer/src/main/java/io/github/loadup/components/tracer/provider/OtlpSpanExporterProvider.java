package io.github.loadup.components.tracer.provider;

/*-
 * #%L
 * Loadup Components Tracer
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
