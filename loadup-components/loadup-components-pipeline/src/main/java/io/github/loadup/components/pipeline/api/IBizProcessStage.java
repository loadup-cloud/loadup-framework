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
