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
 * Conditional flow-control stage.
 *
 * <p>When {@link #shouldStop} returns {@code true} the executor aborts the remaining pipeline
 * stages immediately (the response stored in the context up to this point is returned).
 * {@link IFinallyStage}s are still executed.
 */
public interface IControlStage extends IStage {

    /**
     * Decide whether the pipeline should stop at this point.
     *
     * @param context the shared pipeline context
     * @return {@code true} to stop early, {@code false} to continue
     */
    boolean shouldStop(PipelineContext context);
}
