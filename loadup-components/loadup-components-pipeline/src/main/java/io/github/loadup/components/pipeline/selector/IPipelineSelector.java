package io.github.loadup.components.pipeline.selector;

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
