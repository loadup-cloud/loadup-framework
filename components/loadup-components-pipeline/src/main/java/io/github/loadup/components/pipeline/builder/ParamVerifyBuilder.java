package io.github.loadup.components.pipeline.builder;

import io.github.loadup.components.pipeline.api.IDataPrepareStage;
import io.github.loadup.components.pipeline.api.IParamVerifyStage;
import io.github.loadup.components.pipeline.api.IProcessStage;
import io.github.loadup.components.pipeline.api.IStage;
import io.github.loadup.components.pipeline.context.PipelineContext;
import io.github.loadup.components.pipeline.tx.ITxInitializer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * First builder step — parameter validation, with optional skip to later steps.
 */
@SuppressWarnings("rawtypes")
public final class ParamVerifyBuilder extends AbstractPipelineBuilder {

    public ParamVerifyBuilder(
            List<Class<? extends IStage>> stages,
            Map<Integer, Consumer> consumerIndexMap,
            Map<Integer, Function> functionIndexMap,
            Map<Integer, BiConsumer> biConsumerIndexMap) {
        super(stages, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /** Add a Spring-bean parameter-validation stage. */
    public DataPrepareBuilder verify(Class<? extends IParamVerifyStage<?>> stage) {
        stageList.add(stage);
        return new DataPrepareBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /** Add an inline (lambda) parameter-validation stage. */
    public <REQ> DataPrepareBuilder verify(Consumer<REQ> consumer) {
        registerParamVerifyConsumer(consumer);
        return new DataPrepareBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /** Skip verify — go directly to data-prepare. */
    public ProcessBuilder prepare(Class<? extends IDataPrepareStage<?>> stage) {
        stageList.add(stage);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /** Skip verify — go directly to data-prepare (lambda variant). */
    public <DATA> ProcessBuilder prepare(Function<PipelineContext, DATA> function) {
        registerDataPrepareFunction(function);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /** Skip verify + prepare — go directly to process. */
    public ProcessBuilder process(Class<? extends IProcessStage> stage) {
        stageList.add(stage);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /** Skip verify + prepare — inline biz-process lambda. */
    public ProcessBuilder process(Consumer<PipelineContext> consumer) {
        registerBizProcessConsumer(consumer);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /** Skip verify + prepare — inline data-process lambda. */
    public <DATA> ProcessBuilder process(BiConsumer<DATA, PipelineContext> biConsumer) {
        registerDataProcessBiConsumer(biConsumer);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /** Start a transactional block before any other stage. */
    public ProcessInTxBuilder startTx(Class<? extends ITxInitializer> txInitializer) {
        stageList.add(txInitializer);
        return new ProcessInTxBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }
}
