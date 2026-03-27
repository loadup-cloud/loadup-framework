package io.github.loadup.components.pipeline.stage.function;

import io.github.loadup.components.pipeline.api.IDataProcessStage;
import io.github.loadup.components.pipeline.context.PipelineContext;
import java.util.function.BiConsumer;

/** Lambda adapter for {@link IDataProcessStage}. */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class InnerDataProcessBiConsumerStage implements IFunctionStage, IDataProcessStage {

    private final BiConsumer<Object, PipelineContext> biConsumer;

    public InnerDataProcessBiConsumerStage(BiConsumer<?, PipelineContext> biConsumer) {
        this.biConsumer = (BiConsumer<Object, PipelineContext>) biConsumer;
    }

    @Override
    public void process(Object data, PipelineContext context) {
        biConsumer.accept(data, context);
    }
}
