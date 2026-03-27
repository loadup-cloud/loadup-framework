package io.github.loadup.components.pipeline.stage.function;

import io.github.loadup.components.pipeline.api.IResultAssembleStage;
import io.github.loadup.components.pipeline.context.PipelineContext;
import java.util.function.Function;

/** Lambda adapter for {@link IResultAssembleStage}. */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class InnerResultAssembleFunctionStage implements IFunctionStage, IResultAssembleStage {

    private final Function<PipelineContext, Object> function;

    public InnerResultAssembleFunctionStage(Function<PipelineContext, ?> function) {
        this.function = (Function<PipelineContext, Object>) function;
    }

    @Override
    public Object assemble(PipelineContext context) {
        return function.apply(context);
    }
}
