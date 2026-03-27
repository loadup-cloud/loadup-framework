package io.github.loadup.components.pipeline.exception;

/**
 * Exception handler bound to a specific {@link Throwable} sub-class.
 *
 * <p>Register instances via
 * {@link ExceptionHandlerBus#register(Class, Class)}.
 *
 * @param <RES> the response type
 */
public interface IExceptionClassHandler<RES> extends IExceptionHandler<RES> {}
