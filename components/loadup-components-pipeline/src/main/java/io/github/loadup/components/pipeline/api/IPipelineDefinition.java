package io.github.loadup.components.pipeline.api;

import io.github.loadup.components.pipeline.exception.ExceptionHandlerBus;
import io.github.loadup.components.pipeline.spec.PipelineSpec;

/**
 * Pipeline-definition SPI.
 *
 * <p>Implement this interface (as a Spring {@code @Service} or {@code @Component} bean)
 * to declare a complete pipeline for a business use-case:
 *
 * <pre>{@code
 * @Service
 * public class CreateOrderPipelineDefinition implements IPipelineDefinition {
 *
 *     @Override
 *     public PipelineSpec definePipeline() {
 *         return PipelineBuilder.builder()
 *             .verify(CreateOrderParamVerifyStage.class)
 *             .prepare(OrderDataPrepareStage.class)
 *             .process(CreateOrderProcessStage.class)
 *             .assemble(CreateOrderResultAssembleStage.class)
 *             .build();
 *     }
 *
 *     @Override
 *     public ExceptionHandlerBus exceptions() {
 *         return ExceptionHandlerBus.builder()
 *             .register(BizException.class, OrderBizExceptionHandler.class)
 *             .bottom(OrderSystemExceptionHandler.class);
 *     }
 * }
 * }</pre>
 */
public interface IPipelineDefinition {

    /**
     * Declare the ordered list of pipeline stages.
     *
     * @return the {@link PipelineSpec} describing the pipeline
     */
    PipelineSpec definePipeline();

    /**
     * Declare exception handlers for this pipeline.
     *
     * @return a configured {@link ExceptionHandlerBus}
     */
    ExceptionHandlerBus exceptions();
}
