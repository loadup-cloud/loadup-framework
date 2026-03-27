package io.github.loadup.components.pipeline.stage.function;

import io.github.loadup.components.pipeline.api.IBizProcessStage;
import io.github.loadup.components.pipeline.context.PipelineContext;
import java.util.function.Consumer;

/** Lambda adapter for {@link IBizProcessStage}. */
public final class InnerBizProcessConsumerStage implements IFunctionStage, IBizProcessStage {

    private final Consumer<PipelineContext> consumer;

    public InnerBizProcessConsumerStage(Consumer<PipelineContext> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void process(PipelineContext context) {
        consumer.accept(context);
    }
}
