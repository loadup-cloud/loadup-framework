package io.github.loadup.components.pipeline.api;

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
 * Result-assembly stage — maps domain model to the response DTO.
 *
 * <p>The return value is stored in {@link PipelineContext} and is returned by
 * {@link io.github.loadup.components.pipeline.engine.PipelineExecutor#execute} as the final result.
 *
 * @param <RES> the response type
 */
public interface IResultAssembleStage<RES> extends IStage {

    /**
     * Assemble the response from the pipeline context.
     *
     * @param context the shared pipeline context (contains model, request, properties)
     * @return the response object; must not be {@code null}
     */
    RES assemble(PipelineContext context);
}
