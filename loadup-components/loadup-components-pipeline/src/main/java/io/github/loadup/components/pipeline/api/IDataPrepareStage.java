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
