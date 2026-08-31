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
import io.github.loadup.components.pipeline.context.PipelineContext;
import io.github.loadup.components.pipeline.stage.function.InnerBizProcessConsumerStage;
import io.github.loadup.components.pipeline.stage.function.InnerDataPrepareFunctionStage;
import io.github.loadup.components.pipeline.stage.function.InnerDataProcessBiConsumerStage;
import io.github.loadup.components.pipeline.stage.function.InnerParamVerifyConsumerStage;
import io.github.loadup.components.pipeline.stage.function.InnerResultAssembleFunctionStage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base class for all pipeline builder steps.
 *
 * <p>Each builder step is type-safe: the return type forces the caller
 * through the intended stage order (verify → prepare → process → assemble).
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractPipelineBuilder {

    protected final List<Class<? extends IStage>> stageList;
    protected final Map<Integer, Consumer> consumerIndexMap;
    protected final Map<Integer, Function> functionIndexMap;
    protected final Map<Integer, BiConsumer> biConsumerIndexMap;

    protected AbstractPipelineBuilder(
            List<Class<? extends IStage>> stageList,
            Map<Integer, Consumer> consumerIndexMap,
            Map<Integer, Function> functionIndexMap,
            Map<Integer, BiConsumer> biConsumerIndexMap) {
        this.stageList = stageList;
        this.consumerIndexMap = consumerIndexMap;
        this.functionIndexMap = functionIndexMap;
        this.biConsumerIndexMap = biConsumerIndexMap;
    }

    protected AbstractPipelineBuilder(List<Class<? extends IStage>> stageList) {
        this.stageList = stageList;
        this.consumerIndexMap = new HashMap<>();
        this.functionIndexMap = new HashMap<>();
        this.biConsumerIndexMap = new HashMap<>();
    }

    // ── Lambda registration helpers ──────────────────────────────────────────

    protected <REQ> void registerParamVerifyConsumer(Consumer<REQ> consumer) {
        int idx = stageList.size();
        stageList.add(InnerParamVerifyConsumerStage.class);
        consumerIndexMap.put(idx, consumer);
    }

    protected <DATA> void registerDataPrepareFunction(Function<PipelineContext, DATA> function) {
        int idx = stageList.size();
        stageList.add(InnerDataPrepareFunctionStage.class);
        functionIndexMap.put(idx, function);
    }

    protected <DATA> void registerDataProcessBiConsumer(BiConsumer<DATA, PipelineContext> biConsumer) {
        int idx = stageList.size();
        stageList.add(InnerDataProcessBiConsumerStage.class);
        biConsumerIndexMap.put(idx, biConsumer);
    }

    protected void registerBizProcessConsumer(Consumer<PipelineContext> consumer) {
        int idx = stageList.size();
        stageList.add(InnerBizProcessConsumerStage.class);
        consumerIndexMap.put(idx, consumer);
    }

    protected <RES> void registerResultAssembleFunction(Function<PipelineContext, RES> function) {
        int idx = stageList.size();
        stageList.add(InnerResultAssembleFunctionStage.class);
        functionIndexMap.put(idx, function);
    }
}
