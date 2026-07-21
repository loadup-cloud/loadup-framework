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
 * Pure-business-logic processing stage.
 *
 * <p>Use this stage when the processing step does not need a typed domain model reference —
 * it operates directly on the {@link PipelineContext} (e.g. invoking an extension point,
 * sending a notification, writing an audit log).
 *
 * <p>Use {@link IDataProcessStage} when you need typed access to the prepared domain model.
 */
public interface IBizProcessStage extends IProcessStage {

    /**
     * Execute the business logic.
     *
     * @param context the shared pipeline context
     */
    void process(PipelineContext context);
}
