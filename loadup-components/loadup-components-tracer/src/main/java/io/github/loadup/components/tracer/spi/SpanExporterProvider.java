package io.github.loadup.components.tracer.spi;

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
