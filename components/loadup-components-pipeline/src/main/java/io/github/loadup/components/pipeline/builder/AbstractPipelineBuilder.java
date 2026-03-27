package io.github.loadup.components.pipeline.builder;

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
