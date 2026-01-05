package com.github.loadup.components.tracer.config;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configuration properties for the tracer component. */
@Data
@Validated
@ConfigurationProperties(prefix = "loadup.tracer")
public class TracerProperties {

  /** Enable or disable the tracer. */
  private boolean enabled = true;

  /** Enable web request tracing. */
  private boolean enableWebTracing = true;

  /** Enable async task tracing. */
  private boolean enableAsyncTracing = true;

  /** OTLP exporter endpoint (e.g., http://localhost:4317). */
  @Pattern(
      regexp = "^(https?://)?([a-zA-Z0-9_.-]+|\\[[0-9a-fA-F:]+\\])(:[0-9]+)?(/.*)?$",
      message = "OTLP endpoint must be a valid URL format")
  private String otlpEndpoint;

  /** Include request headers in spans. */
  private boolean includeHeaders = false;

  /** Include request parameters in spans. */
  private boolean includeParameters = false;

  /** URL patterns to exclude from tracing (comma-separated). */
  private String excludePatterns = "/actuator/**,/health,/metrics";
}
