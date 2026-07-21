package io.github.loadup.components.pipeline.selector;

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
