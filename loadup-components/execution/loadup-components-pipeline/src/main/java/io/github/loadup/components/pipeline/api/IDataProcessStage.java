package io.github.loadup.components.pipeline.api;

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
