package io.github.loadup.components.pipeline.api;

/*-
 * #%L
 * Loadup Components Pipeline
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
