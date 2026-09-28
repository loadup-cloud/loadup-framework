package io.github.loadup.components.pipeline.api;

import io.github.loadup.components.pipeline.context.PipelineContext;

/**
 * Result-assembly stage — maps domain model to the response DTO.
 *
 * <p>The return value is stored in {@link PipelineContext} and is returned by
 * {@link io.github.loadup.components.pipeline.engine.PipelineExecutor#execute} as the final result.
 *
 * @param <RES> the response type
 */
public interface IResultAssembleStage<RES> extends IStage {

    /**
     * Assemble the response from the pipeline context.
     *
     * @param context the shared pipeline context (contains model, request, properties)
     * @return the response object; must not be {@code null}
     */
    RES assemble(PipelineContext context);
}
