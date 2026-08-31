package io.github.loadup.components.pipeline.builder;

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
