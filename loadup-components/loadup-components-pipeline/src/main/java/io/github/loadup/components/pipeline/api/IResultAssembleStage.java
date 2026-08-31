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
