package io.github.loadup.components.pipeline.stage.function;

import io.github.loadup.components.pipeline.api.IStage;

/**
 * Marker interface for inline (lambda-based) pipeline stages.
 *
 * <p>These are never registered as Spring beans — they are constructed directly by the
 * {@link io.github.loadup.components.pipeline.engine.PipelineExecutor} from the lambda
 * captured in {@link io.github.loadup.components.pipeline.spec.PipelineSpec}.
 */
public interface IFunctionStage extends IStage {}
