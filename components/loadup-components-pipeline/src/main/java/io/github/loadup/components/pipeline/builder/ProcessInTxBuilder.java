package io.github.loadup.components.pipeline.builder;

import io.github.loadup.components.pipeline.api.IDataPrepareStage;
import io.github.loadup.components.pipeline.api.IProcessStage;
import io.github.loadup.components.pipeline.api.IStage;
import io.github.loadup.components.pipeline.context.PipelineContext;
import io.github.loadup.components.pipeline.tx.EndTxMarker;
import io.github.loadup.components.pipeline.tx.ITxInitializer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builder step used inside an open transactional block.
 * The TX ends when {@link #endTx()} is called, which returns a {@link ProcessBuilder}.
 */
@SuppressWarnings("rawtypes")
public final class ProcessInTxBuilder extends AbstractPipelineBuilder {

    public ProcessInTxBuilder(
            List<Class<? extends IStage>> stages,
            Map<Integer, Consumer> consumerIndexMap,
            Map<Integer, Function> functionIndexMap,
            Map<Integer, BiConsumer> biConsumerIndexMap) {
        super(stages, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /** Chain a Spring-bean process stage inside the TX block. */
    public ProcessInTxBuilder process(Class<? extends IProcessStage> stage) {
        stageList.add(stage);
        return this;
    }

    /** Chain an inline biz-process lambda inside the TX block. */
    public ProcessInTxBuilder process(Consumer<PipelineContext> consumer) {
        registerBizProcessConsumer(consumer);
        return this;
    }

    /** Chain an inline data-process lambda inside the TX block. */
    public <DATA> ProcessInTxBuilder process(BiConsumer<DATA, PipelineContext> biConsumer) {
        registerDataProcessBiConsumer(biConsumer);
        return this;
    }

    /** Chain a data-prepare stage inside the TX block. */
    public ProcessInTxBuilder prepare(Class<? extends IDataPrepareStage<?>> stage) {
        stageList.add(stage);
        return this;
    }

    /** Chain an inline data-prepare lambda inside the TX block. */
    public <DATA> ProcessInTxBuilder prepare(Function<PipelineContext, DATA> function) {
        registerDataPrepareFunction(function);
        return this;
    }

    /** Nest another TX initialiser (e.g. for savepoint semantics). */
    public ProcessInTxBuilder startTx(Class<? extends ITxInitializer> txInitializer) {
        stageList.add(txInitializer);
        return this;
    }

    /** Close the transactional block and return to the outer process builder. */
    public ProcessBuilder endTx() {
        stageList.add(EndTxMarker.class);
        return new ProcessBuilder(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }
}
