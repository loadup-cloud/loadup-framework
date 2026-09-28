package io.github.loadup.components.pipeline.api;

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
