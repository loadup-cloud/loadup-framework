package io.github.loadup.components.pipeline.api;

/*-
 * #%L
 * Loadup Components Pipeline
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
