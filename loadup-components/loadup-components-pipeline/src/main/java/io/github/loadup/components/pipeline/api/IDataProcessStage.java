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
 * Data-processing stage that receives the typed domain model produced by {@link IDataPrepareStage}.
 *
 * <p>Multiple {@code IDataProcessStage} implementations can be chained; each one receives
 * the same model object from the context. Mutations to the model are visible to later stages
 * because the model is stored by reference.
 *
 * @param <DATA> the domain-model type expected by this stage
 */
public interface IDataProcessStage<DATA> extends IProcessStage {

    /**
     * Process the domain model.
     *
     * @param data    the domain model loaded by {@link IDataPrepareStage}
     * @param context the shared pipeline context
     */
    void process(DATA data, PipelineContext context);
}
