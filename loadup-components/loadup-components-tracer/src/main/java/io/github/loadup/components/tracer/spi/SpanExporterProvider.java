package io.github.loadup.components.tracer.spi;

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
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * SPI for plugging in custom OpenTelemetry {@link SpanExporter} implementations.
 *
 * <p>Built-in types: {@code logging}, {@code otlp}, {@code zipkin}, {@code noop}.
 * Additional types can be registered by placing a
 * {@code META-INF/services/io.github.loadup.components.tracer.spi.SpanExporterProvider}
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
