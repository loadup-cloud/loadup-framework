package io.github.loadup.components.pipeline.api;

/*-
 * #%L
 * Loadup Components Pipeline
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
