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
