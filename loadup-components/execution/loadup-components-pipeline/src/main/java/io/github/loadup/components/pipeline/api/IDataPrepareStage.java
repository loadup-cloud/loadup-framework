package io.github.loadup.components.pipeline.api;

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
