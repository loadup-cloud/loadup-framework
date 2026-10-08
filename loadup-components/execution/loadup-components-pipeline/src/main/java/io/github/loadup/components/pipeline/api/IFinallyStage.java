package io.github.loadup.components.pipeline.api;

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
