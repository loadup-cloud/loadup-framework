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
 * Data-preparation stage SPI.
 *
 * <p>Loads the domain model (or any required data) from the repository / downstream service
 * and returns it. The executor stores the returned value in
 * {@link PipelineContext} and makes it available to subsequent {@link IDataProcessStage}s
 * via {@link PipelineContext#getModel(Class)}.
 *
 * @param <DATA> the type of the domain model produced by this stage
 */
public interface IDataPrepareStage<DATA> extends IStage {

    /**
     * Load and return the domain model needed by downstream stages.
     *
     * @param context the shared pipeline context
     * @return the prepared model; must not be {@code null}
     */
    DATA prepare(PipelineContext context);
}
