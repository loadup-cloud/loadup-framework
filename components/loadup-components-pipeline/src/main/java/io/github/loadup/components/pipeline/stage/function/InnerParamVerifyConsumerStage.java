package io.github.loadup.components.pipeline.stage.function;

import io.github.loadup.components.pipeline.api.IParamVerifyStage;
import java.util.function.Consumer;

/** Lambda adapter for {@link IParamVerifyStage}. */
public final class InnerParamVerifyConsumerStage<REQ> implements IFunctionStage, IParamVerifyStage<REQ> {

    private final Consumer<REQ> consumer;

    public InnerParamVerifyConsumerStage(Consumer<REQ> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void verify(REQ req) {
        consumer.accept(req);
    }
}
