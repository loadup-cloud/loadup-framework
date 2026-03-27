package io.github.loadup.components.pipeline.builder;

import io.github.loadup.components.pipeline.api.IStage;
import io.github.loadup.components.pipeline.spec.PipelineSpec;
import io.github.loadup.components.pipeline.tx.EndTxMarker;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Final builder step — terminates the pipeline definition.
 */
@SuppressWarnings("rawtypes")
public final class ResultAssembleBuilder extends AbstractPipelineBuilder {

    public ResultAssembleBuilder(
            List<Class<? extends IStage>> stages,
            Map<Integer, Consumer> consumerIndexMap,
            Map<Integer, Function> functionIndexMap,
            Map<Integer, BiConsumer> biConsumerIndexMap) {
        super(stages, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }

    /** Close a previously opened transactional block after the assemble stage. */
    public ResultAssembleBuilder endTx() {
        stageList.add(EndTxMarker.class);
        return this;
    }

    /** Build the immutable {@link PipelineSpec}. */
    public PipelineSpec build() {
        return new PipelineSpec(stageList, consumerIndexMap, functionIndexMap, biConsumerIndexMap);
    }
}
