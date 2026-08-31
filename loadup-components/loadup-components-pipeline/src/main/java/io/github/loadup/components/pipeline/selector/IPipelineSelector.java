package io.github.loadup.components.pipeline.selector;

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

import io.github.loadup.components.pipeline.api.IPipelineDefinition;

/**
 * SPI for selecting a {@link IPipelineDefinition} at runtime.
 *
 * <p>Use this when multiple pipelines handle the same use-case but differ by
 * {@code bizCode} / tenant / product type. Implement and register as a Spring bean;
 * the {@link io.github.loadup.components.pipeline.engine.PipelineExecutor} can be
 * called with the resolved definition.
 *
 * @param <CTX> the selection-context type (e.g. a request DTO or BizScenario)
 */
public interface IPipelineSelector<CTX> {

    /**
     * Select the pipeline definition appropriate for the given context.
     *
     * @param selectionContext the context used to determine which pipeline to use
     * @return the matching pipeline definition; never {@code null}
     */
    IPipelineDefinition select(CTX selectionContext);
}
