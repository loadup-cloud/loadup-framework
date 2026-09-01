package io.github.loadup.common.tracer.provider;

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

import io.github.loadup.common.tracer.config.TracerProperties.ExporterConfig;
import io.github.loadup.common.tracer.spi.SpanExporterProvider;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporterBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;

/**
 * Exports spans to a Zipkin-compatible backend (e.g. Zipkin, Jaeger with Zipkin receiver).
 *
 * <p>Default endpoint: {@code http://localhost:9411/api/v2/spans}.
 */
@SuppressWarnings("deprecation")
public class ZipkinSpanExporterProvider implements SpanExporterProvider {

    @Override
    public String getType() {
        return "zipkin";
    }

    @Override
    public SpanExporter createExporter(ExporterConfig config) {
        ZipkinSpanExporterBuilder builder =
                ZipkinSpanExporter.builder().setReadTimeout(Duration.ofMillis(config.getTimeout()));
        if (config.getEndpoint() != null && !config.getEndpoint().isBlank()) {
            builder.setEndpoint(config.getEndpoint());
        }
        return builder.build();
    }
}
