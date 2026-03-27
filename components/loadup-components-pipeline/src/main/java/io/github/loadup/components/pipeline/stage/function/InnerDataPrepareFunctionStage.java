package io.github.loadup.components.pipeline.stage.function;

import io.github.loadup.components.pipeline.api.IDataPrepareStage;
import io.github.loadup.components.pipeline.context.PipelineContext;
import java.util.function.Function;

/** Lambda adapter for {@link IDataPrepareStage}. */
@SuppressWarnings("rawtypes")
public final class InnerDataPrepareFunctionStage implements IFunctionStage, IDataPrepareStage {

    private final Function<PipelineContext, Object> function;

    @SuppressWarnings("unchecked")
    public InnerDataPrepareFunctionStage(Function<PipelineContext, ?> function) {
        this.function = (Function<PipelineContext, Object>) function;
    }

    @Override
    public Object prepare(PipelineContext context) {
        return function.apply(context);
    }
}
