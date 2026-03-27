package io.github.loadup.components.pipeline.api;

/**
 * Parameter-validation stage SPI.
 *
 * <p>The stage receives the raw request object and should throw a {@link RuntimeException}
 * (e.g. {@code IllegalArgumentException}) when validation fails, which will then be routed
 * to the configured {@link io.github.loadup.components.pipeline.exception.IExceptionClassHandler}.
 *
 * @param <REQ> request type
 */
public interface IParamVerifyStage<REQ> extends IStage {

    /**
     * Validate the incoming request.
     *
     * @param req the raw request; never {@code null} when called by the pipeline executor
     */
    void verify(REQ req);
}
