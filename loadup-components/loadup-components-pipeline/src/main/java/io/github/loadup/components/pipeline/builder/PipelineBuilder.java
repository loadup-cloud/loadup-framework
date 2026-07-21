package io.github.loadup.components.pipeline.builder;

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

import io.github.loadup.components.pipeline.api.IStage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Entry point for the fluent pipeline-definition DSL.
 *
 * <pre>{@code
 * PipelineSpec spec = PipelineBuilder.builder()
 *     .verify(CreateOrderVerifyStage.class)     // or .verify(req -> { ... })
 *     .prepare(OrderDataPrepareStage.class)      // or .prepare(ctx -> loadOrder(ctx))
 *     .process(CreateOrderProcessStage.class)   // or .process(ctx -> { ... })
 *     .assemble(OrderResultAssembleStage.class)  // or .assemble(ctx -> toDTO(...))
 *     .build();
 * }</pre>
 */
@SuppressWarnings("rawtypes")
public final class PipelineBuilder extends AbstractPipelineBuilder {

    private PipelineBuilder(
            List<Class<? extends IStage>> stages,
            Map<Integer, Consumer> consumerIndexMap,
            Map<Integer, Function> functionIndexMap,
            Map<Integer, BiConsumer> biConsumerIndexMap) {
        super(stages, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /**
     * Start building a new pipeline.
     *
     * @return the first builder step ({@link ParamVerifyBuilder})
     */
    public static ParamVerifyBuilder builder() {
        return new ParamVerifyBuilder(new ArrayList<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }
}
