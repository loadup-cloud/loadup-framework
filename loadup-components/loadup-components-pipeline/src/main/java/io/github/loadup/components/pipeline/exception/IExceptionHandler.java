package io.github.loadup.components.pipeline.exception;

import io.github.loadup.components.pipeline.context.PipelineContext;

/**
 * Top-level exception-handler SPI.
 *
 * @param <RES> the response type returned on exception
 */
public interface IExceptionHandler<RES> {

    /**
     * Side-effect hook called before building the error response (e.g. logging, alerting).
     * Default implementation is a no-op.
     *
     * @param t       the exception
     * @param context the pipeline context at the point of failure
     */
    default void handleException(Throwable t, PipelineContext context) {}

    /**
     * Build the error response.
     *
     * @param t       the exception
     * @param context the pipeline context at the point of failure
     * @return the error response; must not be {@code null}
     */
    RES assembleResultOnException(Throwable t, PipelineContext context);
}
