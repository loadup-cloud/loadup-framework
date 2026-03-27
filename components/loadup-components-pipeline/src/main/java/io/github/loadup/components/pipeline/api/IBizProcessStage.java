package io.github.loadup.components.pipeline.api;

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
