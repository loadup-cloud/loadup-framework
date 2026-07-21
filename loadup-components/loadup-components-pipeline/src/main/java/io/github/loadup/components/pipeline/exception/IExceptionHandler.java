package io.github.loadup.components.pipeline.exception;

/*-
 * #%L
 * Loadup Components Pipeline
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

import io.github.loadup.components.pipeline.context.PipelineContext;

/**
 * Top-level exception-handler SPI.
 *
 * @param <RES> the response type returned on exception
 */
public interface IExceptionHandler<RES> {

    /**
     * Side-effect hook called before building the error response (e.g. logging, alerting).
     * Default implementation is a no-op.
     *
     * @param t       the exception
     * @param context the pipeline context at the point of failure
     */
    default void handleException(Throwable t, PipelineContext context) {}

    /**
     * Build the error response.
     *
     * @param t       the exception
     * @param context the pipeline context at the point of failure
     * @return the error response; must not be {@code null}
     */
    RES assembleResultOnException(Throwable t, PipelineContext context);
}
