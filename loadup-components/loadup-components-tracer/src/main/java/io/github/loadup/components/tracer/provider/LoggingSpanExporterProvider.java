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
