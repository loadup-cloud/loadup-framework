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
 * Cleanup stage that is always executed — even if the pipeline throws an exception.
 *
 * <p>Analogous to a {@code finally} block. Use for releasing resources, writing audit logs, etc.
 * Exceptions thrown by a {@code IFinallyStage} are swallowed and logged at WARN level.
 */
public interface IFinallyStage extends IStage {

    /**
     * Run cleanup logic.
     *
     * @param context the shared pipeline context
     */
    void doFinally(PipelineContext context);
}
